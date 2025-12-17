-- Create triggers to automatically maintain category article count

DELIMITER //

-- Trigger after article insert
CREATE TRIGGER after_article_insert
AFTER INSERT ON t_article
FOR EACH ROW
BEGIN
    IF NEW.category_id IS NOT NULL THEN
        UPDATE t_category
        SET article_count = IFNULL(article_count, 0) + 1
        WHERE id = NEW.category_id;
    END IF;
END//

-- Trigger after article update (category change)
CREATE TRIGGER after_article_update
AFTER UPDATE ON t_article
FOR EACH ROW
BEGIN
    -- Category changed
    IF NEW.category_id != OLD.category_id THEN
        -- Decrease old category count
        IF OLD.category_id IS NOT NULL THEN
            UPDATE t_category
            SET article_count = GREATEST(IFNULL(article_count, 0) - 1, 0)
            WHERE id = OLD.category_id;
        END IF;

        -- Increase new category count
        IF NEW.category_id IS NOT NULL THEN
            UPDATE t_category
            SET article_count = IFNULL(article_count, 0) + 1
            WHERE id = NEW.category_id;
        END IF;
    END IF;
END//

-- Trigger after article delete
CREATE TRIGGER after_article_delete
AFTER DELETE ON t_article
FOR EACH ROW
BEGIN
    IF OLD.category_id IS NOT NULL THEN
        UPDATE t_category
        SET article_count = GREATEST(IFNULL(article_count, 0) - 1, 0)
        WHERE id = OLD.category_id;
    END IF;
END//

DELIMITER ;