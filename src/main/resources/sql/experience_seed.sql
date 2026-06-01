-- ============================================================
-- 经验金库 · 好看的种子数据（演示用）
-- 说明：作者统一挂到当前库里的第一个用户，保证外键有效；
--      卡片本身不展示作者名，看起来就是社区里大家投的故事。
--      想换成不同作者，把 @AUTHOR 换成具体 user_id 即可。
-- ============================================================

SET @AUTHOR = (SELECT user_id FROM users ORDER BY user_id LIMIT 1);

INSERT INTO experience_posts(user_id, title, summary, icon, theme, tags, cover_image, created_at) VALUES
(@AUTHOR,
 '确诊那天，我在医院走廊坐了一下午',
 '肠镜报告出来的那一刻，世界安静得可怕。后来我才明白，确诊不是终点，而是终于知道对手是谁、可以开始打这场仗了。写给每一个刚听到"克罗恩"三个字的你。',
 '🔥', 'midnight', '血泪史,共鸣',
 'https://images.unsplash.com/photo-1516574187841-cb9cc2ca948b?w=900&q=80',
 DATE_SUB(NOW(), INTERVAL 1 HOUR)),

(@AUTHOR,
 '我把"无麸质"这件事，过成了一种生活美学',
 '不能吃的清单很长，但我开始研究能吃的：糙米饭团、清蒸鲈鱼、自己熬的南瓜小米粥。三个月下来体重稳住了，复查指标也降了。原来好好吃饭本身，就是最硬的治疗。',
 '🌿', 'aurora', '治愈,清单',
 'https://images.unsplash.com/photo-1490645935967-10de6ba17061?w=900&q=80',
 DATE_SUB(NOW(), INTERVAL 6 HOUR)),

(@AUTHOR,
 '生物制剂打了半年，我重新跑起来了',
 '从一口气爬不上三楼，到现在能沿着江边慢跑五公里。用药这条路很贵也很折腾，但身体一点点找回力气的感觉，真的会让人想哭。把我的用药节奏和踩过的坑都写在这儿。',
 '💊', 'editorial', '治愈,用药',
 'https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=900&q=80',
 DATE_SUB(NOW(), INTERVAL 1 DAY)),

(@AUTHOR,
 '半夜偷吃了一串烧烤，第二天我后悔哭了',
 '防线破产那一晚，嘴是爽了，肚子却抗议了整整两天。写下来不是为了自责，是想告诉同样会嘴馋的你：我们都会犯错，重要的是第二天还能把营养粉端起来，继续往前走。',
 '😈', 'sunrise', '血泪史,克制',
 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=900&q=80',
 DATE_SUB(NOW(), INTERVAL 2 DAY)),

(@AUTHOR,
 '出院那天，护士站的姐姐塞给我一颗糖',
 '住院两周，挂水挂到手背都肿了。出院结账时，那个总来查房的护士偷偷塞给我一颗水果糖，说"出去好好的"。有些温柔，会被你记一辈子。',
 '🌙', 'aurora', '治愈,日常',
 'https://images.unsplash.com/photo-1538108149393-fbbd81895907?w=900&q=80',
 DATE_SUB(NOW(), INTERVAL 3 DAY)),

(@AUTHOR,
 '给新病友的一封信：你不是一个人在扛',
 '我曾经以为这是只属于我一个人的羞耻和孤独，直到走进这个社区，看到一墙的战友都在亮着自己的状态。被看见的那一刻，那股没办法的孤独，真的会被消解掉。',
 '🧠', 'editorial', '人生感悟,共鸣',
 'https://images.unsplash.com/photo-1499209974431-9dddcece7f88?w=900&q=80',
 DATE_SUB(NOW(), INTERVAL 5 DAY)),

(@AUTHOR,
 '我的随身急救包，装了三年终于成型',
 '蒙脱石散、口服补液盐、保温的小热水袋、还有一张写着主治医生电话的卡片。出门在外最怕突然发作，这份清单是我用无数次狼狈换来的，分享给同样需要安全感的你。',
 '🚑', 'sunrise', '清单,日常',
 'https://images.unsplash.com/photo-1603398938378-e54eab446dde?w=900&q=80',
 DATE_SUB(NOW(), INTERVAL 7 DAY));
