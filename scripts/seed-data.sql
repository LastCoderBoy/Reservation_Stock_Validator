-- ========================================
-- LIMITED STOCK DROP - SAMPLE DATA
-- ========================================
-- Run this AFTER schema.sql to populate sample data

-- ========================================
-- SAMPLE USERS
-- ========================================

INSERT INTO users (
    first_name,
    last_name,
    email,
    username,
    password,
    active,
    role,
    updated_at
) VALUES
    ('John', 'Doe', 'john.doe@example.com', 'johndoe',
     '$2a$12$JWp6SvpMRTO7LCSW.Gz0EOkvklsH6Stu/jKhUCsNgE2JnGIShK3wS', TRUE, 'ROLE_USER', NOW()),
    ('Jane', 'Smith', 'jane.smith@example.com', 'janesmith',
     '$2a$12$yXmvxeSscMPWtEkLvV6doOVfLyzJjgU8cn4gKYtXD9VBk.pflebaC', TRUE, 'ROLE_USER', NOW()),
    ('Bob', 'Williams', 'bob.williams@example.com', 'bobwill',
     '$2a$12$eJFxGn16RtFVEcEo3KfbJuY0MeIPo4Vao9LylLT9o3yL0u4QXU8Mm', TRUE, 'ROLE_ADMIN', NOW()),
    ('Charlie', 'Brown', 'charlie.brown@example.com', 'charlieb',
     '$2a$12$wQJqYxdr8JTRTjUsMrUI6OPIFmvQG8k0yeP8mABsXOhz1IBPu4cki', TRUE, 'ROLE_USER', NOW());


-- ========================================
-- SAMPLE PRODUCTS
-- ========================================
-- Categories: MEN, WOMEN, KIDS

INSERT INTO products (
    name,
    description,
    price,
    image_key,
    total_stock,
    reserved_stock,
    category,
    active,
    created_at,
    updated_at,
    version
) VALUES
      -- MEN
      ('Slim Fit Dress Shirt', 'White 100% cotton dress shirt, slim fit, perfect for formal occasions.', 49.99, 'mens_dress_shirt.jpg', 80, 0, 'MEN', TRUE, NOW(), NOW(), 0),
      ('Casual Polo Shirt', 'Navy polo shirt, breathable cotton, versatile for casual wear.', 39.99, 'mens_polo_shirt.jpg', 90, 0, 'MEN', TRUE, NOW(), NOW(), 0),
      ('Wool Overcoat', 'Charcoal wool overcoat, lined, classic winter essential.', 179.99, 'mens_wool_overcoat.jpg', 40, 0, 'MEN', TRUE, NOW(), NOW(), 0),
      ('Denim Jeans', 'Dark blue denim jeans, stretch fabric, modern fit.', 69.99, 'mens_denim_jeans.jpg', 100, 0, 'MEN', TRUE, NOW(), NOW(), 0),
      ('Cotton T-Shirt (5-Pack)', 'Assorted colors, 100% cotton, comfortable everyday wear.', 34.99, 'mens_cotton_tees.jpg', 120, 0, 'MEN', TRUE, NOW(), NOW(), 0),
      ('Linen Button-Up Shirt', 'Beige linen shirt, breathable, ideal for summer.', 59.99, 'mens_linen_shirt.jpg', 70, 0, 'MEN', TRUE, NOW(), NOW(), 0),
      ('Fleece Hoodie', 'Gray fleece hoodie, soft fabric, perfect for casual wear.', 49.99, 'mens_fleece_hoodie.jpg', 90, 0, 'MEN', TRUE, NOW(), NOW(), 0),
      ('Chinos', 'Khaki chinos, stretch fabric, versatile for work or casual.', 54.99, 'mens_chinos.jpg', 85, 0, 'MEN', TRUE, NOW(), NOW(), 0),
      ('Sweater Vest', 'Navy sweater vest, wool blend, stylish layering piece.', 44.99, 'mens_sweater_vest.jpg', 60, 0, 'MEN', TRUE, NOW(), NOW(), 0),
      ('Cargo Pants', 'Olive cargo pants, durable fabric, multiple pockets.', 59.99, 'mens_cargo_pants.jpg', 75, 0, 'MEN', TRUE, NOW(), NOW(), 0),

      -- WOMEN
      ('Silk Blouse', 'Floral print silk blouse, elegant and breathable.', 69.99, 'womens_silk_blouse.jpg', 70, 0, 'WOMEN', TRUE, NOW(), NOW(), 0),
      ('High-Waisted Skinny Jeans', 'Blue stretch denim, high-waisted, flattering fit.', 59.99, 'womens_skinny_jeans.jpg', 90, 0, 'WOMEN', TRUE, NOW(), NOW(), 0),
      ('Knit Cardigan', 'Cream knit cardigan, soft wool blend, cozy and stylish.', 54.99, 'womens_knit_cardigan.jpg', 60, 0, 'WOMEN', TRUE, NOW(), NOW(), 0),
      ('Summer Sundress', 'Yellow floral sundress, lightweight fabric, perfect for summer.', 49.99, 'womens_sundress.jpg', 80, 0, 'WOMEN', TRUE, NOW(), NOW(), 0),
      ('Leather Jacket', 'Black faux leather jacket, edgy and warm for fall.', 129.99, 'womens_leather_jacket.jpg', 50, 0, 'WOMEN', TRUE, NOW(), NOW(), 0),
      ('Lace Blouse', 'White lace blouse, delicate design, perfect for special occasions.', 79.99, 'womens_lace_blouse.jpg', 45, 0, 'WOMEN', TRUE, NOW(), NOW(), 0),
      ('Maxi Skirt', 'Black maxi skirt, flowy fabric, versatile for any season.', 39.99, 'womens_maxi_skirt.jpg', 70, 0, 'WOMEN', TRUE, NOW(), NOW(), 0),
      ('Turtleneck Sweater', 'Beige turtleneck sweater, soft wool, warm and stylish.', 64.99, 'womens_turtleneck_sweater.jpg', 55, 0, 'WOMEN', TRUE, NOW(), NOW(), 0),
      ('Jumpsuit', 'Navy jumpsuit, stretch fabric, elegant and comfortable.', 74.99, 'womens_jumpsuit.jpg', 40, 0, 'WOMEN', TRUE, NOW(), NOW(), 0),
      ('Cropped Jacket', 'Olive cropped jacket, lightweight, perfect for layering.', 59.99, 'womens_cropped_jacket.jpg', 65, 0, 'WOMEN', TRUE, NOW(), NOW(), 0),

      -- KIDS
      ('Graphic T-Shirt (Dinosaur)', 'Cotton t-shirt with dinosaur print, fun and comfortable.', 14.99, 'kids_dino_tee.jpg', 100, 0, 'KIDS', TRUE, NOW(), NOW(), 0),
      ('Denim Overalls', 'Blue denim overalls, adjustable straps, durable for play.', 29.99, 'kids_denim_overalls.jpg', 80, 0, 'KIDS', TRUE, NOW(), NOW(), 0),
      ('Hooded Sweatshirt', 'Pink hoodie, soft fleece, warm and cozy.', 24.99, 'kids_hooded_sweatshirt.jpg', 90, 0, 'KIDS', TRUE, NOW(), NOW(), 0),
      ('Cotton Pajama Set', 'Comfortable pajama set with cartoon prints, 100% cotton.', 19.99, 'kids_pajama_set.jpg', 70, 0, 'KIDS', TRUE, NOW(), NOW(), 0),
      ('Fleece Jacket', 'Blue fleece jacket, lightweight and warm for outdoor play.', 39.99, 'kids_fleece_jacket.jpg', 60, 0, 'KIDS', TRUE, NOW(), NOW(), 0),
      ('Polka Dot Dress', 'Red polka dot dress, cotton fabric, perfect for summer.', 22.99, 'kids_polka_dot_dress.jpg', 75, 0, 'KIDS', TRUE, NOW(), NOW(), 0),
      ('Striped T-Shirt', 'Blue and white striped t-shirt, soft cotton, casual wear.', 12.99, 'kids_striped_tee.jpg', 85, 0, 'KIDS', TRUE, NOW(), NOW(), 0),
      ('Leggings', 'Black leggings, stretchy fabric, comfortable for play.', 16.99, 'kids_leggings.jpg', 95, 0, 'KIDS', TRUE, NOW(), NOW(), 0),
      ('Button-Up Shirt', 'White button-up shirt, cotton, perfect for school or events.', 19.99, 'kids_button_up_shirt.jpg', 80, 0, 'KIDS', TRUE, NOW(), NOW(), 0),
      ('Sweater', 'Green  sweater, wool blend, warm and cozy for winter.', 27.99, 'kids_sweater.jpg', 65, 0, 'KIDS', TRUE, NOW(), NOW(), 0);
