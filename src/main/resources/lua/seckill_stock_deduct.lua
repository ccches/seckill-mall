-- 秒杀库存原子扣减
-- KEYS[1] = seckill:stock:{activityId}
-- KEYS[2] = seckill:user:{activityId}:{userId}
-- ARGV[1] = orderNo
-- 返回值: 1=成功, -2=库存不足, -3=重复秒杀

local stock = tonumber(redis.call('GET', KEYS[1]))
if stock == nil or stock <= 0 then
    return -2
end

if redis.call('GET', KEYS[2]) ~= false then
    return -3
end

redis.call('DECR', KEYS[1])
redis.call('SET', KEYS[2], ARGV[1], 'EX', 3600)

return 1
