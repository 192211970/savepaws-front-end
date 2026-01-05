-- =============================================================
-- AUTO ESCALATION STORED PROCEDURES AND EVENTS
-- =============================================================
-- 
-- INSTRUCTIONS FOR phpMyAdmin:
-- 1. Go to your database 'savepaws' in phpMyAdmin
-- 2. Click on the "SQL" tab
-- 3. At the bottom of the page, find "Delimiter" dropdown
-- 4. Change it from ";" to "$$"
-- 5. Paste ONE procedure at a time
-- 6. Click "Go"
-- 7. Repeat for each procedure
-- 8. For EVENTs, change delimiter back to ";"
--
-- =============================================================

-- =============================================================
-- PROCEDURE 1: Escalate Delayed Cases
-- =============================================================
-- Copy from here to the next separator, set delimiter to $$

DROP PROCEDURE IF EXISTS sp_escalate_delayed_cases$$

CREATE PROCEDURE sp_escalate_delayed_cases()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_case_id, v_user_id, v_center_id INT;
    DECLARE v_lat, v_lng DECIMAL(10,7);
    
    -- Cursor for delayed cases (Reported status, > 60 min old, no Sent_again)
    DECLARE cur CURSOR FOR
        SELECT c.case_id, c.user_id, c.latitude, c.longitude
        FROM cases c
        WHERE c.status = 'Reported'
          AND TIMESTAMPDIFF(MINUTE, c.created_time, NOW()) >= 60
          AND NOT EXISTS (
              SELECT 1 FROM case_escalations ce 
              WHERE ce.case_id = c.case_id 
              AND ce.remark = 'Sent_again'
              AND ce.status = 'Pending'
          );
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO v_case_id, v_user_id, v_lat, v_lng;
        IF done THEN LEAVE read_loop; END IF;
        
        -- Find nearest active center
        SET v_center_id = NULL;
        SELECT center_id INTO v_center_id
        FROM centers
        WHERE is_active = 1 OR is_active = 'Yes'
        ORDER BY (6371 * ACOS(
            COS(RADIANS(v_lat)) * COS(RADIANS(latitude)) *
            COS(RADIANS(longitude) - RADIANS(v_lng)) +
            SIN(RADIANS(v_lat)) * SIN(RADIANS(latitude))
        )) ASC
        LIMIT 1;
        
        IF v_center_id IS NOT NULL THEN
            -- Mark old escalations as Resent
            UPDATE case_escalations
            SET remark = 'Resent'
            WHERE case_id = v_case_id 
            AND remark IN ('Delayed', 'Sent_again', 'None');
            
            -- Insert new escalation
            INSERT INTO case_escalations 
                (user_id, case_id, center_id, status, response, rejected_reason, remark, case_type, assigned_time)
            VALUES 
                (v_user_id, v_case_id, v_center_id, 'Pending', NULL, NULL, 'Sent_again', 'Critical', NOW());
        END IF;
    END LOOP;
    
    CLOSE cur;
END$$

-- =============================================================
-- PROCEDURE 2: Escalate Rejected-by-all Cases  
-- =============================================================
-- Copy from here to the next separator, set delimiter to $$

DROP PROCEDURE IF EXISTS sp_escalate_rejected_cases$$

CREATE PROCEDURE sp_escalate_rejected_cases()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_case_id, v_user_id, v_center_id INT;
    DECLARE v_lat, v_lng DECIMAL(10,7);
    
    -- Cursor for rejected-by-all cases that haven't been sent again
    DECLARE cur CURSOR FOR
        SELECT DISTINCT ce.case_id
        FROM case_escalations ce
        WHERE ce.remark = 'Rejected_by_all'
          AND NOT EXISTS (
              SELECT 1 FROM case_escalations ce2 
              WHERE ce2.case_id = ce.case_id 
              AND ce2.remark = 'Sent_again'
              AND ce2.status = 'Pending'
          );
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    OPEN cur;
    
    read_loop: LOOP
        FETCH cur INTO v_case_id;
        IF done THEN LEAVE read_loop; END IF;
        
        -- Get case info
        SELECT user_id, latitude, longitude INTO v_user_id, v_lat, v_lng
        FROM cases WHERE case_id = v_case_id;
        
        -- Find best-performing center within 25km
        SET v_center_id = NULL;
        SELECT center_id INTO v_center_id
        FROM centers
        WHERE (is_active = 1 OR is_active = 'Yes')
          AND (6371 * ACOS(
              COS(RADIANS(v_lat)) * COS(RADIANS(latitude)) *
              COS(RADIANS(longitude) - RADIANS(v_lng)) +
              SIN(RADIANS(v_lat)) * SIN(RADIANS(latitude))
          )) <= 25
        ORDER BY total_cases_handled DESC
        LIMIT 1;
        
        IF v_center_id IS NOT NULL THEN
            -- Mark old as Resent
            UPDATE case_escalations
            SET remark = 'Resent'
            WHERE case_id = v_case_id AND remark = 'Rejected_by_all';
            
            -- Insert new escalation
            INSERT INTO case_escalations 
                (user_id, case_id, center_id, status, response, rejected_reason, remark, case_type, assigned_time)
            VALUES 
                (v_user_id, v_case_id, v_center_id, 'Pending', NULL, NULL, 'Sent_again', 'Critical', NOW());
        END IF;
    END LOOP;
    
    CLOSE cur;
END$$


-- =============================================================
-- EVENTS (Run after procedures are created)
-- =============================================================
-- For these, set delimiter BACK to ";" in phpMyAdmin

-- First, enable event scheduler (run this once)
SET GLOBAL event_scheduler = ON;

-- Drop existing events if any
DROP EVENT IF EXISTS auto_escalate_delayed;
DROP EVENT IF EXISTS auto_escalate_rejected;

-- Event for delayed cases (runs every 5 minutes)
CREATE EVENT auto_escalate_delayed
ON SCHEDULE EVERY 5 MINUTE
STARTS CURRENT_TIMESTAMP
ON COMPLETION PRESERVE
ENABLE
DO CALL sp_escalate_delayed_cases();

-- Event for rejected cases (runs every 5 minutes)
CREATE EVENT auto_escalate_rejected
ON SCHEDULE EVERY 5 MINUTE
STARTS CURRENT_TIMESTAMP
ON COMPLETION PRESERVE
ENABLE
DO CALL sp_escalate_rejected_cases();


-- =============================================================
-- VERIFICATION QUERIES (Run to verify setup)
-- =============================================================

-- Check if event_scheduler is ON
-- SHOW VARIABLES LIKE 'event_scheduler';

-- Check if procedures exist
-- SHOW PROCEDURE STATUS WHERE Db = 'savepaws';

-- Check if events exist
-- SHOW EVENTS FROM savepaws;

-- Manually test procedures (optional)
-- CALL sp_escalate_delayed_cases();
-- CALL sp_escalate_rejected_cases();
