---@diagnostic disable: undefined-global
-- 1.参数列表
-- 1.1优惠券ID
local collectionId = ARGV[1]
-- 1.2用户ID
local userId = ARGV[2]
-- 1.3订单id
local orderId = ARGV[3]

-- 2.数据key
-- 2.1库存key
local stockKey = "limited:stock:" .. collectionId
-- 2.2订单key,值是set集合(保存用户id)
local orderKey = "limited:order:" .. collectionId

-- 3.脚本业务
-- 3.1判断库存是否充足：先判断是否为nil，再转数字（关键修复）
local stock = redis.call('get', stockKey)
-- 修复逻辑：stock为nil（键不存在）或转数字后<=0，均视为库存不足
if not stock or tonumber(stock) <= 0 then
    return 1
end
-- 3.2判断用户是否已下单 sismember orderKey userId
if(tonumber(redis.call('sismember',orderKey,userId)) == 1)then
    -- 3.3用户已下单
    return 2
end

-- 3.4扣减库存 invrby stocKey -1
redis.call('incrby',stockKey,-1)
-- 3.5下单,保存用户 sadd orderKey userId
redis.call('sadd',orderKey,userId)
-- 3.6发送消息到队列中，xadd stream.orders * k1 v1 k2 v2
redis.call('xadd','stream.orders','*','userId',userId,'collectionId',collectionId,'id',orderId)
return 0