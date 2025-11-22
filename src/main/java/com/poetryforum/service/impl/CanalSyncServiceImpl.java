package com.poetryforum.service.impl;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.CanalEntry.*;
import com.alibaba.otter.canal.protocol.Message;
import com.poetryforum.dto.esdto.CollectionEsDTO;
import com.poetryforum.dto.esdto.PoemEsDTO;
import com.poetryforum.entity.Poem;
import com.poetryforum.entity.PoetryCollection;
import com.poetryforum.esdao.CollectionEsDao;
import com.poetryforum.esdao.PoemEsDao;
import com.poetryforum.service.IPoemService;
import com.poetryforum.service.IPoetryCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class CanalSyncServiceImpl implements com.poetryforum.service.CanalSyncService {

    @Resource
    private PoemEsDao poemEsDao;

    @Resource
    private IPoemService poemService;

    @Resource
    private CollectionEsDao collectionEsDao;

    @Resource
    private IPoetryCollectionService poetryCollectionService;

    @Value("${canal.server.host:127.0.0.1}")
    private String canalHost;

    @Value("${canal.server.port:11111}")
    private int canalPort;

    @Value("${canal.destination:example}")
    private String destination;

    private volatile boolean running = false;
    private ExecutorService executorService;
    private CanalConnector connector;

    @Override
    public void start() {
        running = true;
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(this::canalListen);
        log.info("Canal同步服务启动成功");
    }

    private void canalListen() {
        try {
            connector = CanalConnectors.newSingleConnector(
                    new InetSocketAddress(canalHost, canalPort),
                    destination, "", "");
            connector.connect();
            // 只订阅poem表和collection表的变更
            connector.subscribe("pf_db.tb_poem,pf_db.tb_collection");
            connector.rollback();

            int batchSize = 1000;
            while (running) {
                Message message = connector.getWithoutAck(batchSize);
                long batchId = message.getId();
                int size = message.getEntries().size();

                if (batchId == -1 || size == 0) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // 中断退出
                        if (!running) {
                            break;
                        }
                    }
                } else {
                    processEntries(message.getEntries());
                }

                connector.ack(batchId);
            }
        } catch (Exception e) {
            log.error("Canal监听异常", e);
        } finally {
            if (connector != null) {
                connector.disconnect();
            }
            log.info("Canal同步服务已停止");
        }
    }

    private void processEntries(List<Entry> entries) {
        for (Entry entry : entries) {
            if (entry.getEntryType() == EntryType.TRANSACTIONBEGIN ||
                    entry.getEntryType() == EntryType.TRANSACTIONEND) {
                continue;
            }

            RowChange rowChange;
            try {
                rowChange = RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                log.error("解析binlog异常", e);
                continue;
            }

            EventType eventType = rowChange.getEventType();
            String tableName = entry.getHeader().getTableName();

            // 只处理poem表
            if ("tb_poem".equals(tableName)) {
                processPoemTable(rowChange, eventType);
            }
            else if ("tb_collection".equals(tableName)) {
                // 只处理collection表
                processCollectionTable(rowChange, eventType);
            }
        }
    }

    private void processPoemTable(RowChange rowChange, EventType eventType) {
        for (RowData rowData : rowChange.getRowDatasList()) {
            if (eventType == EventType.DELETE) {
                // 处理删除
                Long id = getColumnValue(rowData.getBeforeColumnsList(), "id");
                if (id != null) {
                    poemEsDao.deleteById(id);
                    log.info("ES删除诗词: {}", id);
                }
            } else if (eventType == EventType.INSERT || eventType == EventType.UPDATE) {
                // 处理新增和更新
                Long id = getColumnValue(rowData.getAfterColumnsList(), "id");
                if (id != null) {
                    Poem poem = poemService.getById(id);
                    if (poem != null) {
                        PoemEsDTO poemEsDTO = PoemEsDTO.objToDto(poem);
                        poemEsDao.save(poemEsDTO);
                        log.info("ES{}诗词: {}", eventType == EventType.INSERT ? "新增" : "更新", id);
                    }
                }
            }
        }
    }

    // 新增：处理诗集表的变更
    private void processCollectionTable(RowChange rowChange, EventType eventType) {
        for (RowData rowData : rowChange.getRowDatasList()) {
            if (eventType == EventType.DELETE) {
                // 处理删除
                Long id = getColumnValue(rowData.getBeforeColumnsList(), "id");
                if (id != null) {
                    collectionEsDao.deleteById(id);
                    log.info("ES删除诗集: {}", id);
                }
            } else if (eventType == EventType.INSERT || eventType == EventType.UPDATE) {
                // 处理新增和更新
                Long id = getColumnValue(rowData.getAfterColumnsList(), "id");
                if (id != null) {
                    PoetryCollection collection = poetryCollectionService.getById(id);
                    if (collection != null) {
                        CollectionEsDTO collectionEsDTO = CollectionEsDTO.objToDto(collection);
                        collectionEsDao.save(collectionEsDTO);
                        log.info("ES{}诗集: {}", eventType == EventType.INSERT ? "新增" : "更新", id);
                    }
                }
            }
        }
    }

    private Long getColumnValue(List<Column> columns, String columnName) {
        for (Column column : columns) {
            if (columnName.equals(column.getName())) {
                return Long.parseLong(column.getValue());
            }
        }
        return null;
    }

    @Override
    public void stop() {
        running = false;
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public int getPhase() {
        // 确保在所有Bean初始化完成后启动
        return Integer.MAX_VALUE;
    }
}