-- thống kê check in, check out, tổng giờ làm
SELECT 
	S1.user_id,
	S1.date_time AS check_in_at,
	S2.date_time AS check_out_at,
	TIME_FORMAT(SEC_TO_TIME(TIMESTAMPDIFF(SECOND, S1.date_time, S2.date_time)), '%H:%i:%s') AS total_time
FROM structure AS S1 
INNER JOIN structure AS S2 ON 
	S1.user_id = S2.user_id
	AND DATE(S1.date_time) = DATE(S2.date_time) 
WHERE 
	(S1.status = 'CHECK_IN' OR S1.status = 'CHECK_IN_LATE')
	AND (S2.status = 'CHECK_OUT' OR S2.status = 'CHECK_OUT_EARLY') 
ORDER BY S1.date_time 
DESC


-- Hiển thị danh sách lỗi checkin, checkout của các nhân viên trong tháng
SELECT 
	u.id AS user_id, 
	MAX(CASE WHEN s.status = 'CHECK_IN_LATE' OR s.status = 'CHECK_IN_MISSING' THEN s.date_time ELSE NULL END) AS check_in_at,
	MAX(CASE WHEN s.status = 'CHECK_OUT_EARLY' THEN s.date_time ELSE NULL END) AS check_out_at,
	GROUP_CONCAT(s.status, '') AS status_error
FROM structure s
JOIN user u ON s.user_id = u.id
WHERE s.date_time BETWEEN '2023-07-01' AND '2023-07-31'
	AND (s.status = 'CHECK_IN_LATE' OR s.status = 'CHECK_OUT_EARLY' OR s.status = 'CHECK_IN_MISSING')
GROUP BY u.id, DATE(s.date_time)
ORDER BY DATE(s.date_time);

-- Hiển thị danh sách lỗi checkin, checkout của 1 nhân viên trong tháng
SELECT 
	u.id AS user_id, 
	MAX(CASE WHEN s.status = 'CHECK_IN_LATE' OR s.status = 'CHECK_IN_MISSING' THEN s.date_time ELSE NULL END) AS check_in_at,
	MAX(CASE WHEN s.status = 'CHECK_OUT_EARLY' THEN s.date_time ELSE NULL END) AS check_out_at,
	GROUP_CONCAT(s.status, '') AS status_error
FROM structure s
JOIN user u ON s.user_id = u.id
WHERE s.date_time BETWEEN '2023-07-01' AND '2023-07-31'
	AND (s.status = 'CHECK_IN_LATE' OR s.status = 'CHECK_OUT_EARLY' OR s.status = 'CHECK_IN_MISSING')
    AND u.id = 6267
GROUP BY u.id, DATE(s.date_time)
ORDER BY DATE(s.date_time);

-- thống kê check in, check out của cá nhân
SELECT 
	S1.user_id,
	S1.date_time AS check_in_at,
	S2.date_time AS check_out_at,
	TIME_FORMAT(SEC_TO_TIME(TIMESTAMPDIFF(SECOND, S1.date_time, S2.date_time)), '%H:%i:%s') AS total_time
FROM structure AS S1 
INNER JOIN structure AS S2 ON 
	S1.user_id = S2.user_id
	AND DATE(S1.date_time) = DATE(S2.date_time) 
WHERE 
	S1.date_time BETWEEN '2023-07-10' AND '2023-07-13'
	AND (S1.status = 'CHECK_IN' OR S1.status = 'CHECK_IN_LATE')
	AND (S2.status = 'CHECK_OUT' OR S2.status = 'CHECK_OUT_EARLY') 
    AND S1.user_id = 6267
ORDER BY S1.date_time 
DESC







