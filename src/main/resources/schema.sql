-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : sam. 25 avr. 2026 à 00:39
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `nutrilife_db`
--

-- --------------------------------------------------------

--
-- Structure de la table `additives_danger`
--

CREATE TABLE `additives_danger` (
  `id` int(11) NOT NULL,
  `code` varchar(10) NOT NULL,
  `name` varchar(150) NOT NULL,
  `danger_level` int(11) NOT NULL CHECK (`danger_level` between 1 and 10),
  `category` varchar(100) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `health_effects` text DEFAULT NULL,
  `banned_countries` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `additives_danger`
--

INSERT INTO `additives_danger` (`id`, `code`, `name`, `danger_level`, `category`, `description`, `health_effects`, `banned_countries`) VALUES
(1, 'E100', 'Curcumin', 1, 'Color', 'Natural yellow color extracted from turmeric', 'No known adverse effects', NULL),
(2, 'E101', 'Riboflavin (Vitamin B2)', 1, 'Color', 'Natural yellow vitamin', 'No adverse effects, essential vitamin', NULL),
(3, 'E140', 'Chlorophyll', 1, 'Color', 'Natural green color from plants', 'No known adverse effects', NULL),
(4, 'E160a', 'Beta-Carotene', 1, 'Color', 'Natural orange color, vitamin A precursor', 'No adverse effects', NULL),
(5, 'E162', 'Beetroot Red', 1, 'Color', 'Natural red color from beetroot', 'No known adverse effects', NULL),
(6, 'E170', 'Calcium Carbonate', 2, 'Mineral', 'Natural calcium source', 'Generally safe in normal doses', NULL),
(7, 'E175', 'Gold', 2, 'Color', 'Edible gold leaf for decoration', 'Safe in food-grade form', NULL),
(8, 'E300', 'Ascorbic Acid (Vitamin C)', 1, 'Antioxidant', 'Natural vitamin C', 'No adverse effects, essential vitamin', NULL),
(9, 'E306', 'Tocopherols (Vitamin E)', 1, 'Antioxidant', 'Natural vitamin E', 'No adverse effects', NULL),
(10, 'E322', 'Lecithin', 2, 'Emulsifier', 'Natural emulsifier from soy or eggs', 'Safe except for soy/egg allergies', NULL),
(11, 'E330', 'Citric Acid', 1, 'Acidifier', 'Natural acid found in citrus fruits', 'No adverse effects', NULL),
(12, 'E331', 'Sodium Citrate', 2, 'Acidifier', 'Sodium salt of citric acid', 'Safe in normal consumption', NULL),
(13, 'E334', 'Tartaric Acid', 2, 'Acidifier', 'Natural acid from grapes', 'Safe in normal doses', NULL),
(14, 'E406', 'Agar', 1, 'Thickener', 'Natural gelling agent from seaweed', 'No adverse effects', NULL),
(15, 'E410', 'Locust Bean Gum', 2, 'Thickener', 'Natural thickener from carob seeds', 'Safe, may cause allergies in rare cases', NULL),
(16, 'E412', 'Guar Gum', 2, 'Thickener', 'Natural thickener from guar beans', 'Safe in normal doses', NULL),
(17, 'E414', 'Gum Arabic', 2, 'Thickener', 'Natural gum from acacia tree', 'No adverse effects', NULL),
(18, 'E440', 'Pectin', 1, 'Thickener', 'Natural gelling agent from fruits', 'No adverse effects', NULL),
(19, 'E500', 'Sodium Bicarbonate', 2, 'Raising Agent', 'Baking soda', 'Safe in normal consumption', NULL),
(20, 'E901', 'Beeswax', 2, 'Glazing Agent', 'Natural wax from bees', 'Generally safe', NULL),
(21, 'E102', 'Tartrazine', 6, 'Color', 'Synthetic yellow color', 'May cause hyperactivity in children, allergic reactions', 'Restricted in some countries'),
(22, 'E104', 'Quinoline Yellow', 6, 'Color', 'Synthetic yellow color', 'Hyperactivity, allergic reactions', 'Banned in USA, Norway'),
(23, 'E110', 'Sunset Yellow', 6, 'Color', 'Synthetic orange color', 'Hyperactivity in children, allergies', 'Banned in Norway, Finland'),
(24, 'E122', 'Carmoisine', 6, 'Color', 'Synthetic red color', 'Hyperactivity, allergic reactions', 'Banned in USA, Japan, Norway'),
(25, 'E124', 'Ponceau 4R', 6, 'Color', 'Synthetic red color', 'Hyperactivity, possible carcinogen', 'Banned in USA, Norway'),
(26, 'E129', 'Allura Red', 6, 'Color', 'Synthetic red color', 'Hyperactivity, allergic reactions', 'Banned in some European countries'),
(27, 'E150d', 'Caramel IV (Sulfite Ammonia)', 5, 'Color', 'Industrial caramel coloring', 'Contains 4-MEI, possible carcinogen', NULL),
(28, 'E211', 'Sodium Benzoate', 5, 'Preservative', 'Synthetic preservative', 'Hyperactivity, may form benzene with vitamin C', NULL),
(29, 'E220', 'Sulfur Dioxide', 5, 'Preservative', 'Used in dried fruits and wine', 'Asthma triggers, allergic reactions', NULL),
(30, 'E223', 'Sodium Metabisulfite', 5, 'Preservative', 'Sulfite preservative', 'Asthma, allergic reactions', NULL),
(31, 'E249', 'Potassium Nitrite', 6, 'Preservative', 'Used in cured meats', 'May form nitrosamines (carcinogenic)', NULL),
(32, 'E250', 'Sodium Nitrite', 6, 'Preservative', 'Used in cured meats and sausages', 'May form nitrosamines, linked to cancer risk', 'Restricted in some countries'),
(33, 'E251', 'Sodium Nitrate', 6, 'Preservative', 'Used in cured meats', 'Converts to nitrites, cancer risk', NULL),
(34, 'E320', 'BHA (Butylated Hydroxyanisole)', 6, 'Antioxidant', 'Synthetic antioxidant', 'Possible human carcinogen', 'Banned in Japan'),
(35, 'E321', 'BHT (Butylated Hydroxytoluene)', 6, 'Antioxidant', 'Synthetic antioxidant', 'Possible carcinogen, hormone disruption', 'Banned in some countries'),
(36, 'E407', 'Carrageenan', 5, 'Thickener', 'Extracted from red seaweed', 'Inflammation, digestive issues', NULL),
(37, 'E466', 'Carboxymethyl Cellulose (CMC)', 5, 'Thickener', 'Synthetic thickener', 'Gut microbiome disruption, inflammation', NULL),
(38, 'E471', 'Mono and Diglycerides', 5, 'Emulsifier', 'May contain trans fats', 'Possible cardiovascular effects', NULL),
(39, 'E621', 'Monosodium Glutamate (MSG)', 5, 'Flavor Enhancer', 'Synthetic flavor enhancer', 'Headaches, nausea, \"Chinese restaurant syndrome\"', NULL),
(40, 'E627', 'Disodium Guanylate', 5, 'Flavor Enhancer', 'Often combined with MSG', 'Should be avoided by people with gout', NULL),
(41, 'E631', 'Disodium Inosinate', 5, 'Flavor Enhancer', 'Often combined with MSG', 'Should be avoided by people with gout', NULL),
(42, 'E120', 'Cochineal / Carmine', 7, 'Color', 'Color extracted from insects', 'Severe allergic reactions, anaphylaxis', NULL),
(43, 'E127', 'Erythrosine', 8, 'Color', 'Synthetic red color', 'Thyroid issues, possible carcinogen', 'Banned in Norway, USA cosmetics'),
(44, 'E128', 'Red 2G', 9, 'Color', 'Synthetic red color', 'Carcinogenic, banned in EU', 'Banned in EU since 2007'),
(45, 'E131', 'Patent Blue V', 7, 'Color', 'Synthetic blue color', 'Severe allergic reactions, tumors in animals', 'Banned in USA, Australia'),
(46, 'E132', 'Indigotine', 7, 'Color', 'Synthetic blue color', 'Allergic reactions, hyperactivity', 'Banned in Norway'),
(47, 'E133', 'Brilliant Blue FCF', 7, 'Color', 'Synthetic blue color', 'Allergies, ADHD symptoms', 'Banned in some European countries'),
(48, 'E142', 'Green S', 7, 'Color', 'Synthetic green color', 'Allergic reactions, asthma', 'Banned in USA, Canada, Norway'),
(49, 'E151', 'Brilliant Black BN', 7, 'Color', 'Synthetic black color', 'Allergic reactions, intestinal issues', 'Banned in USA, Canada, Norway'),
(50, 'E171', 'Titanium Dioxide', 9, 'Color', 'White color, contains nanoparticles', 'DNA damage, possible carcinogen', 'Banned in France (2020), EU (2022)'),
(51, 'E173', 'Aluminum', 8, 'Color', 'Metallic silver coloring', 'Linked to Alzheimer\'s, neurotoxicity', 'Restricted in many countries'),
(52, 'E210', 'Benzoic Acid', 7, 'Preservative', 'Synthetic preservative', 'Hyperactivity, asthma, may form benzene', NULL),
(53, 'E212', 'Potassium Benzoate', 7, 'Preservative', 'Synthetic preservative', 'Hyperactivity, allergic reactions', NULL),
(54, 'E213', 'Calcium Benzoate', 7, 'Preservative', 'Synthetic preservative', 'Hyperactivity, allergic reactions', NULL),
(55, 'E284', 'Boric Acid', 9, 'Preservative', 'Toxic preservative', 'Reproductive toxicity, organ damage', 'Banned in many countries'),
(56, 'E285', 'Sodium Tetraborate (Borax)', 9, 'Preservative', 'Toxic preservative', 'Reproductive toxicity, hormone disruption', 'Banned in many countries'),
(57, 'E924', 'Potassium Bromate', 10, 'Flour Treatment', 'Used in flour treatment', 'Confirmed carcinogen, kidney damage', 'Banned in EU, UK, Canada, Brazil'),
(58, 'E925', 'Chlorine', 9, 'Flour Treatment', 'Used to bleach flour', 'Carcinogenic byproducts, respiratory issues', 'Banned in EU'),
(59, 'E927b', 'Carbamide (Urea)', 8, 'Flour Treatment', 'Used in chewing gum', 'Liver and kidney issues', NULL),
(60, 'E950', 'Acesulfame Potassium', 7, 'Sweetener', 'Artificial sweetener', 'Possible carcinogen, metabolic effects', NULL),
(61, 'E951', 'Aspartame', 8, 'Sweetener', 'Artificial sweetener', 'Possible carcinogen (WHO 2023), headaches, neurological effects', NULL),
(62, 'E952', 'Cyclamate', 8, 'Sweetener', 'Artificial sweetener', 'Possible carcinogen, banned in USA', 'Banned in USA since 1969'),
(63, 'E954', 'Saccharin', 7, 'Sweetener', 'Artificial sweetener', 'Possible carcinogen, bladder cancer concerns', 'Restricted in some countries'),
(64, 'E955', 'Sucralose', 7, 'Sweetener', 'Artificial sweetener', 'Gut microbiome disruption, DNA damage concerns', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `badge`
--

CREATE TABLE `badge` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `description` varchar(255) NOT NULL,
  `condition_text` varchar(255) NOT NULL,
  `condition_type` varchar(50) NOT NULL,
  `condition_value` int(11) NOT NULL,
  `svg` varchar(30) NOT NULL,
  `couleur` varchar(20) NOT NULL,
  `couleur_bg` varchar(20) NOT NULL,
  `categorie` varchar(50) NOT NULL,
  `ordre` int(11) NOT NULL,
  `rarete` varchar(20) NOT NULL DEFAULT 'common',
  `icon` varchar(20) DEFAULT NULL,
  `name` varchar(100) NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `badge`
--

INSERT INTO `badge` (`id`, `nom`, `description`, `condition_text`, `condition_type`, `condition_value`, `svg`, `couleur`, `couleur_bg`, `categorie`, `ordre`, `rarete`, `icon`, `name`) VALUES
(1, 'First Recipe', 'Create your very first recipe', 'Create 1 recipe', 'recipes_count', 1, 'sprout', '#2f9e44', '#e8fbe8', 'cuisine', 1, 'common', NULL, ''),
(2, 'Home Chef', 'You\'ve created 5 recipes!', 'Create 5 recipes', 'recipes_count', 5, 'medal', '#e67700', '#fff3e0', 'cuisine', 2, 'rare', NULL, ''),
(3, 'Master Chef', 'An impressive collection of 20 recipes', 'Create 20 recipes', 'recipes_count', 20, 'star', '#f59f00', '#fff9db', 'cuisine', 3, 'epic', NULL, ''),
(4, 'Waste Fighter', 'Save your first ingredient from waste', 'Consume 1 ingredient before expiry', 'ingredients_saved', 1, 'leaf', '#37b24d', '#ebfbee', 'anti-waste', 1, 'common', NULL, ''),
(5, 'Eco Warrior', 'Save 25 ingredients from the bin', 'Consume 25 ingredients before expiry', 'ingredients_saved', 25, 'shield', '#1098ad', '#e3fafc', 'anti-waste', 2, 'rare', NULL, ''),
(6, 'Zero Waste Hero', 'Save 100 ingredients — you\'re a legend!', 'Consume 100 ingredients before expiry', 'ingredients_saved', 100, 'star', '#ae3ec9', '#f8f0fc', 'anti-waste', 3, 'legendary', NULL, ''),
(7, 'First Step', 'Track your first ingredient', 'Add 1 ingredient', 'ingredients_tracked', 1, 'sprout', '#5c940d', '#f4fce3', 'tracking', 1, 'common', NULL, ''),
(8, 'Inventory Pro', 'Track 50 ingredients in your kitchen', 'Add 50 ingredients', 'ingredients_tracked', 50, 'medal', '#d9480f', '#fff4e6', 'tracking', 2, 'epic', NULL, ''),
(9, 'On Fire!', '3 consecutive days of activity', 'Be active 3 days in a row', 'streak_days', 3, 'fire', '#e03131', '#ffe3e3', 'streak', 1, 'common', NULL, ''),
(10, 'Unstoppable', '7 consecutive days — a full week!', 'Be active 7 days in a row', 'streak_days', 7, 'fire', '#c92a2a', '#fff5f5', 'streak', 2, 'rare', NULL, ''),
(11, 'Generous Cook', 'Share your first recipe publicly', 'Share 1 recipe', 'shared_recipes', 1, 'shield', '#1c7ed6', '#e7f5ff', 'community', 1, 'rare', NULL, ''),
(101, 'First Recipe', 'Create your first recipe', 'Create 1 recipe', 'recipes_count', 1, 'sprout', '#2f9e44', '#e8fbe8', 'level1', 1, 'common', NULL, 'First Recipe'),
(102, 'First Scan', 'Scan your first product', 'Scan 1 product', 'scans_count', 1, 'scan', '#1c7ed6', '#e7f5ff', 'level1', 2, 'common', NULL, 'First Scan'),
(103, 'Boycott Hero', 'Reject 3 boycotted products', 'Reject 3 products', 'boycott_rejects', 3, 'shield', '#e03131', '#ffe3e3', 'level1', 3, 'rare', NULL, 'Boycott Hero'),
(104, 'Meal Plan Champion', 'Complete a full meal plan', 'Complete 1 meal plan', 'meal_plans_completed', 1, 'calendar', '#2f9e44', '#e8fbe8', 'level2', 4, 'rare', NULL, 'Meal Plan Champion'),
(105, 'Master Chef', 'Create 20 recipes', 'Create 20 recipes', 'recipes_count', 20, 'star', '#f59f00', '#fff9db', 'level2', 5, 'epic', NULL, 'Master Chef'),
(106, 'Weekly Warrior', 'Complete all 21 meals in a week', 'Complete 1 full week plan', 'meal_plans_completed', 1, 'fire', '#e67700', '#fff3e0', 'level2', 6, 'epic', NULL, 'Weekly Warrior'),
(107, 'Ingredient Master', 'Add 50 ingredients to your kitchen', 'Track 50 ingredients', 'ingredients_tracked', 50, 'leaf', '#37b24d', '#ebfbee', 'level3', 7, 'epic', NULL, 'Ingredient Master'),
(108, 'Ethical Guardian', 'Reject 10 boycotted products', 'Reject 10 products', 'boycott_rejects', 10, 'shield', '#ae3ec9', '#f8f0fc', 'level3', 8, 'legendary', NULL, 'Ethical Guardian'),
(109, 'Legend Crown', 'Reach 500 ethical points', 'Reach 500 points', 'ethical_points', 500, 'crown', '#f59f00', '#fff9db', 'level3', 9, 'legendary', NULL, 'Legend Crown');

-- --------------------------------------------------------

--
-- Structure de la table `boycott_brands`
--

CREATE TABLE `boycott_brands` (
  `id` int(11) NOT NULL,
  `brand_name` varchar(150) NOT NULL,
  `parent_company` varchar(150) DEFAULT NULL,
  `reason` text NOT NULL,
  `alternatives` text DEFAULT NULL,
  `category` varchar(100) DEFAULT NULL,
  `source_url` varchar(255) DEFAULT NULL,
  `date_added` date DEFAULT curdate()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `boycott_brands`
--

INSERT INTO `boycott_brands` (`id`, `brand_name`, `parent_company`, `reason`, `alternatives`, `category`, `source_url`, `date_added`) VALUES
(1, 'Coca-Cola', 'The Coca-Cola Company', 'Major operations and investments in occupied territories, factory in Atarot settlement', 'Hamoud Boualem, Mecca Cola, Boga Cola', 'Drinks', NULL, '2026-04-17'),
(3, 'Sprite', 'The Coca-Cola Company', 'Subsidiary of Coca-Cola Company', 'Hamoud Boualem Slim, Boga Citron', 'Drinks', NULL, '2026-04-17'),
(4, 'Fanta', 'The Coca-Cola Company', 'Subsidiary of Coca-Cola Company', 'Hamoud Boualem Orange, Boga Orange', 'Drinks', NULL, '2026-04-17'),
(5, 'Nescafé', 'Nestlé', 'Subsidiary of Nestlé', 'Local coffee brands, Café Najjar', 'Drinks', NULL, '2026-04-17'),
(6, 'Nesquik', 'Nestlé', 'Subsidiary of Nestlé', 'Local cocoa brands, Homemade hot chocolate', 'Drinks', NULL, '2026-04-17'),
(7, 'Starbucks', 'Starbucks Corporation', 'CEO public support and donations to occupation', 'Local coffee shops, Independent cafés', 'Drinks', NULL, '2026-04-17'),
(8, 'Red Bull', 'Red Bull GmbH', 'Reported investments supporting occupation', 'Local energy drinks, Natural alternatives', 'Drinks', NULL, '2026-04-17'),
(9, 'Jaffa Juice', 'Israeli Export', 'Directly produced in occupied territories', 'Fresh local juice, Homemade juice', 'Drinks', NULL, '2026-04-17'),
(10, 'Nestlé', 'Nestlé S.A.', 'Investments and factories in occupied territories, owns Israeli companies', 'Local dairy brands, Jaouda', 'Dairy Products', NULL, '2026-04-17'),
(11, 'Danone', 'Danone S.A.', 'Operations and partnerships in occupied territories', 'Jaouda, Local yogurt, Homemade lben', 'Dairy Products', NULL, '2026-04-17'),
(12, 'Activia', 'Danone S.A.', 'Subsidiary of Danone', 'Jaouda yogurt, Local probiotic yogurt', 'Dairy Products', NULL, '2026-04-17'),
(13, 'Danette', 'Danone S.A.', 'Subsidiary of Danone', 'Local desserts, Homemade pudding', 'Dairy Products', NULL, '2026-04-17'),
(14, 'Délice', 'Danone / Délice Group', 'Partnership with Danone in occupied territories', 'Jaouda, Local dairy farms', 'Dairy Products', NULL, '2026-04-17'),
(16, 'La Vache Qui Rit', 'Bel Group', 'Operations in occupied territories', 'Jaouda cheese, Local cheese brands', 'Dairy Products', NULL, '2026-04-17'),
(17, 'Kiri', 'Bel Group', 'Subsidiary of Bel Group with occupation ties', 'Local cream cheese, Homemade labneh', 'Dairy Products', NULL, '2026-04-17'),
(18, 'Président', 'Lactalis', 'Operations and trade with occupied territories', 'Local butter and cheese, Farm products', 'Dairy Products', NULL, '2026-04-17'),
(19, 'Oreo', 'Mondelez International', 'Major operations in occupied territories', 'Bimo Tonik, Local biscuits', 'Snacks', NULL, '2026-04-17'),
(20, 'Milka', 'Mondelez International', 'Subsidiary of Mondelez', 'Aiguebelle, Local chocolate', 'Snacks', NULL, '2026-04-17'),
(21, 'KitKat', 'Nestlé', 'Subsidiary of Nestlé', 'Aiguebelle, Local chocolate bars', 'Snacks', NULL, '2026-04-17'),
(22, 'Nutella', 'Ferrero', 'Reported investments supporting occupation economy', 'Local chocolate spread, Homemade spread', 'Snacks', NULL, '2026-04-17'),
(23, 'Ferrero Rocher', 'Ferrero', 'Reported investments supporting occupation economy', 'Local chocolate, Artisan chocolates', 'Snacks', NULL, '2026-04-17'),
(24, 'Kinder', 'Ferrero', 'Subsidiary of Ferrero', 'Local chocolate, Aiguebelle', 'Snacks', NULL, '2026-04-17'),
(25, 'Kinder Bueno', 'Ferrero', 'Subsidiary of Ferrero', 'Local chocolate bars, Aiguebelle', 'Snacks', NULL, '2026-04-17'),
(26, 'Lays', 'PepsiCo / Frito-Lay', 'Subsidiary of PepsiCo', 'Mr. Chips, Local potato chips', 'Snacks', NULL, '2026-04-17'),
(27, 'Maggi', 'Nestlé', 'Subsidiary of Nestlé', 'Local bouillon cubes, Homemade stock', 'Grocery Store', NULL, '2026-04-17'),
(28, 'Osem', 'Nestlé / Osem', 'Israeli food company owned by Nestlé', 'Local couscous, Dari Couspate', 'Grocery Store', NULL, '2026-04-17'),
(29, 'Kellogg\'s', 'Kellogg Company', 'Reported ties to occupation activities', 'Local cereal brands', 'Grocery Store', NULL, '2026-04-17'),
(30, 'Corn Flakes', 'Kellogg Company', 'Subsidiary of Kellogg', 'Local cereal brands', 'Grocery Store', NULL, '2026-04-17'),
(31, 'McCain', 'McCain Foods', 'Reported ties to occupation economy', 'Local frozen fries, Fresh potatoes', 'Frozen', NULL, '2026-04-17'),
(32, 'Häagen-Dazs', 'General Mills / Nestlé', 'Founded with ties to occupation support', 'Local ice cream, Homemade sorbet', 'Frozen', NULL, '2026-04-17'),
(33, 'Ben & Jerry\'s', 'Unilever', 'Parent company Unilever has occupation ties', 'Local ice cream brands', 'Frozen', NULL, '2026-04-17'),
(34, 'Magnum', 'Unilever', 'Subsidiary of Unilever', 'Local ice cream brands', 'Frozen', NULL, '2026-04-17'),
(35, 'Tabasco', 'McIlhenny Company', 'Reported indirect ties to occupation support', 'Harissa, Local hot sauce', 'Condiments', NULL, '2026-04-17'),
(36, 'HP Sauce', 'Kraft Heinz', 'Subsidiary of Kraft Heinz', 'Homemade sauces, Local brands', 'Condiments', NULL, '2026-04-17'),
(37, 'Schwartz', 'McCormick & Company', 'Reported ties to occupation economy', 'Local spices, Fresh ground spices', 'Condiments', NULL, '2026-04-17'),
(38, 'Jaffa', 'Israeli Export', 'Directly produced in occupied territories', 'Local oranges, Moroccan oranges, Turkish oranges', 'Fruits', NULL, '2026-04-17'),
(39, 'Jaffa Oranges', 'Israeli Export', 'Directly produced in occupied territories', 'Local oranges, Moroccan oranges', 'Fruits', NULL, '2026-04-17'),
(40, 'McDonald\'s', 'McDonald\'s Corporation', 'Offered free meals to Israeli soldiers, franchise in occupied territories', 'Local restaurants, Homemade burgers', 'Fast Food', NULL, '2026-04-17'),
(41, 'KFC', 'Yum! Brands', 'Operations and franchise in occupied territories', 'Local fried chicken restaurants', 'Fast Food', NULL, '2026-04-17'),
(42, 'Pizza Hut', 'Yum! Brands', 'Franchise operations in occupied territories', 'Local pizzerias', 'Fast Food', NULL, '2026-04-17'),
(43, 'Burger King', 'Restaurant Brands International', 'Franchise operations in occupied territories', 'Local burger restaurants', 'Fast Food', NULL, '2026-04-17'),
(44, 'Domino\'s Pizza', 'Domino\'s Inc', 'Franchise operations in occupied territories', 'Local pizzerias', 'Fast Food', NULL, '2026-04-17'),
(45, 'Subway', 'Subway IP LLC', 'Franchise in occupied territories', 'Local sandwich shops', 'Fast Food', NULL, '2026-04-17'),
(46, 'Baskin Robbins', 'Inspire Brands', 'Franchise operations in occupied territories', 'Local ice cream shops', 'Fast Food', NULL, '2026-04-17'),
(47, 'Pepsi', 'PepsiCo', 'Major operations and investments in occupied territories', 'Mecca Cola, Hamoud Boualem', 'Drinks', NULL, '2026-04-17'),
(48, '7UP', 'PepsiCo', 'Subsidiary of PepsiCo', 'Hamoud Boualem Slim, Local sodas', 'Drinks', NULL, '2026-04-17'),
(49, 'Mountain Dew', 'PepsiCo', 'Subsidiary of PepsiCo', 'Local sodas', 'Drinks', NULL, '2026-04-17'),
(50, 'Tropicana', 'PepsiCo', 'Subsidiary of PepsiCo', 'Fresh local juice, Homemade juice', 'Drinks', NULL, '2026-04-17'),
(51, 'Lipton', 'Unilever', 'Subsidiary of Unilever with occupation ties', 'Local tea brands, Herbal teas', 'Drinks', NULL, '2026-04-17'),
(52, 'Minute Maid', 'The Coca-Cola Company', 'Subsidiary of Coca-Cola', 'Fresh local juice', 'Drinks', NULL, '2026-04-17'),
(53, 'Powerade', 'The Coca-Cola Company', 'Subsidiary of Coca-Cola', 'Local sports drinks, Water', 'Drinks', NULL, '2026-04-17'),
(54, 'L\'Oréal', 'L\'Oréal Group', 'Factory in occupied territories, investments supporting occupation', 'Local cosmetics, Natural products', 'Cosmetics', NULL, '2026-04-17'),
(55, 'Loreal', 'L\'Oréal Group', 'Factory in occupied territories, investments supporting occupation', 'Local cosmetics, Natural products', 'Cosmetics', NULL, '2026-04-17'),
(56, 'Garnier', 'L\'Oréal Group', 'Subsidiary of L\'Oréal', 'Local natural cosmetics', 'Cosmetics', NULL, '2026-04-17'),
(57, 'Maybelline', 'L\'Oréal Group', 'Subsidiary of L\'Oréal', 'Local makeup brands', 'Cosmetics', NULL, '2026-04-17'),
(58, 'Lancôme', 'L\'Oréal Group', 'Subsidiary of L\'Oréal', 'Local perfume and cosmetics', 'Cosmetics', NULL, '2026-04-17'),
(59, 'NYX', 'L\'Oréal Group', 'Subsidiary of L\'Oréal', 'Local makeup brands', 'Cosmetics', NULL, '2026-04-17'),
(60, 'Dove', 'Unilever', 'Subsidiary of Unilever with occupation ties', 'Local soap, Natural soap brands', 'Cosmetics', NULL, '2026-04-17'),
(61, 'Axe', 'Unilever', 'Subsidiary of Unilever', 'Local deodorant brands', 'Cosmetics', NULL, '2026-04-17'),
(62, 'Rexona', 'Unilever', 'Subsidiary of Unilever', 'Local deodorant brands', 'Cosmetics', NULL, '2026-04-17'),
(63, 'Lux', 'Unilever', 'Subsidiary of Unilever', 'Local soap brands', 'Cosmetics', NULL, '2026-04-17'),
(64, 'Pantene', 'Procter & Gamble', 'P&G operations and investments in occupied territories', 'Local shampoo brands', 'Cosmetics', NULL, '2026-04-17'),
(65, 'Head & Shoulders', 'Procter & Gamble', 'Subsidiary of P&G', 'Local shampoo brands', 'Cosmetics', NULL, '2026-04-17'),
(66, 'Gillette', 'Procter & Gamble', 'Subsidiary of P&G', 'Local shaving brands', 'Cosmetics', NULL, '2026-04-17'),
(67, 'Oral-B', 'Procter & Gamble', 'Subsidiary of P&G', 'Local toothbrush brands', 'Cosmetics', NULL, '2026-04-17'),
(68, 'Ariel', 'Procter & Gamble', 'Subsidiary of P&G', 'Local detergent brands', 'Cosmetics', NULL, '2026-04-17'),
(69, 'Pampers', 'Procter & Gamble', 'Subsidiary of P&G', 'Local diaper brands', 'Cosmetics', NULL, '2026-04-17'),
(70, 'Johnson & Johnson', 'Johnson & Johnson', 'Reported ties to occupation activities', 'Local baby care brands', 'Cosmetics', NULL, '2026-04-17'),
(71, 'Neutrogena', 'Johnson & Johnson', 'Subsidiary of J&J', 'Local skincare brands', 'Cosmetics', NULL, '2026-04-17'),
(72, 'Nivea', 'Beiersdorf', 'Reported ties to occupation economy', 'Local skincare brands', 'Cosmetics', NULL, '2026-04-17'),
(73, 'Dettol', 'Reckitt', 'Reported ties to occupation activities', 'Local disinfectants', 'Cosmetics', NULL, '2026-04-17'),
(74, 'Clearasil', 'Reckitt', 'Subsidiary of Reckitt', 'Local skincare brands', 'Cosmetics', NULL, '2026-04-17'),
(75, 'Zara', 'Inditex', 'Operations and investments in occupied territories', 'Local clothing brands', 'Fashion', NULL, '2026-04-17'),
(76, 'H&M', 'H&M Group', 'Operations in occupied territories', 'Local clothing brands', 'Fashion', NULL, '2026-04-17'),
(77, 'Puma', 'Kering Group', 'Sponsor of Israeli Football Association', 'Local sports brands, Decathlon', 'Fashion', NULL, '2026-04-17'),
(78, 'Hugo Boss', 'Hugo Boss AG', 'Historical ties and reported current occupation support', 'Local clothing brands', 'Fashion', NULL, '2026-04-17'),
(79, 'Skechers', 'Skechers USA', 'Operations in occupied territories', 'Local shoe brands', 'Fashion', NULL, '2026-04-17'),
(80, 'Timberland', 'VF Corporation', 'Reported ties to occupation economy', 'Local shoe brands', 'Fashion', NULL, '2026-04-17'),
(81, 'HP', 'HP Inc / Hewlett Packard', 'Technology used for occupation surveillance and control systems', 'Local tech brands, Lenovo', 'Technology', NULL, '2026-04-17'),
(82, 'Intel', 'Intel Corporation', 'Major R&D centers in occupied territories, economic support', 'AMD, Local alternatives', 'Technology', NULL, '2026-04-17'),
(83, 'Microsoft', 'Microsoft Corporation', 'Cloud contracts with Israeli military', 'Open source alternatives, Linux', 'Technology', NULL, '2026-04-17'),
(84, 'Google', 'Alphabet Inc', 'Project Nimbus cloud contract with Israeli military', 'DuckDuckGo, Open source alternatives', 'Technology', NULL, '2026-04-17'),
(85, 'Amazon', 'Amazon.com Inc', 'Project Nimbus cloud contract with Israeli military', 'Local e-commerce platforms', 'Technology', NULL, '2026-04-17'),
(86, 'Apple', 'Apple Inc', 'Operations and investments in occupied territories', 'Local alternatives where possible', 'Technology', NULL, '2026-04-17'),
(87, 'Meta', 'Meta Platforms', 'Censorship of Palestinian content, reported ties', 'Telegram, Signal, Local platforms', 'Technology', NULL, '2026-04-17'),
(88, 'Cisco', 'Cisco Systems', 'Technology provided to occupation forces', 'Open source networking', 'Technology', NULL, '2026-04-17'),
(89, 'Motorola', 'Motorola Solutions', 'Communication systems used by occupation forces', 'Local alternatives', 'Technology', NULL, '2026-04-17'),
(90, 'Airbnb', 'Airbnb Inc', 'Listed properties in illegal settlements', 'Local hotels, Local booking platforms', 'Tourism', NULL, '2026-04-17'),
(91, 'TripAdvisor', 'Tripadvisor Inc', 'Listed illegal settlement attractions', 'Local tourism platforms', 'Tourism', NULL, '2026-04-17'),
(92, 'Booking.com', 'Booking Holdings', 'Listed illegal settlement properties', 'Local hotels directly', 'Tourism', NULL, '2026-04-17'),
(93, 'Caterpillar', 'Caterpillar Inc', 'Bulldozers used to demolish Palestinian homes', 'Local machinery brands', 'Industrial', NULL, '2026-04-17'),
(94, 'Volvo', 'Volvo Group', 'Machinery used in occupation infrastructure', 'Local alternatives', 'Industrial', NULL, '2026-04-17'),
(95, 'Siemens', 'Siemens AG', 'Infrastructure projects in occupied territories', 'Local alternatives', 'Industrial', NULL, '2026-04-17'),
(96, 'Pepsico', 'PepsiCo Inc', 'Major operations and investments in occupied territories', 'Mecca Cola, Local brands', 'Drinks', NULL, '2026-04-17'),
(97, 'Unilever', 'Unilever PLC', 'Operations and investments in occupied territories', 'Local brands for all categories', 'Company', NULL, '2026-04-17'),
(98, 'Procter & Gamble', 'P&G', 'Operations and investments in occupied territories', 'Local brands for all categories', 'Company', NULL, '2026-04-17'),
(99, 'Mondelez', 'Mondelez International', 'Operations in occupied territories', 'Local snack brands, Bimo', 'Snacks', NULL, '2026-04-17'),
(100, 'Kraft Heinz', 'Kraft Heinz Company', 'Operations and investments in occupied territories', 'Local condiment brands', 'Condiments', NULL, '2026-04-17'),
(101, 'Heinz', 'Kraft Heinz', 'Subsidiary of Kraft Heinz', 'Local ketchup brands, Homemade sauces', 'Condiments', NULL, '2026-04-17'),
(102, 'Disney', 'The Walt Disney Company', 'Reported pro-occupation stance', 'Local entertainment', 'Entertainment', NULL, '2026-04-17'),
(103, 'Fox', 'Fox Corporation', 'Reported pro-occupation media coverage', 'Independent media', 'Entertainment', NULL, '2026-04-17');

-- --------------------------------------------------------

--
-- Structure de la table `daily_log`
--

CREATE TABLE `daily_log` (
  `id` int(11) NOT NULL,
  `day_number` int(11) NOT NULL,
  `date` date NOT NULL,
  `calories_consumed` int(11) DEFAULT NULL,
  `protein_consumed` double DEFAULT NULL,
  `carbs_consumed` double DEFAULT NULL,
  `fats_consumed` double DEFAULT NULL,
  `water_consumed` double DEFAULT NULL,
  `mood` varchar(50) DEFAULT NULL,
  `selected_foods` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`selected_foods`)),
  `notes` longtext DEFAULT NULL,
  `completed` tinyint(4) NOT NULL,
  `custom_foods` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`custom_foods`)),
  `share_token` varchar(64) DEFAULT NULL,
  `meals` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`meals`)),
  `nutrition_objective_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `daily_log`
--

INSERT INTO `daily_log` (`id`, `day_number`, `date`, `calories_consumed`, `protein_consumed`, `carbs_consumed`, `fats_consumed`, `water_consumed`, `mood`, `selected_foods`, `notes`, `completed`, `custom_foods`, `share_token`, `meals`, `nutrition_objective_id`) VALUES
(1, 1, '2026-04-24', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, '{\"breakfast\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"lunch\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"dinner\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"snacks\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}}}', 2),
(2, 2, '2026-04-25', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, '{\"breakfast\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"lunch\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"dinner\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"snacks\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}}}', 2),
(3, 3, '2026-04-26', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, '{\"breakfast\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"lunch\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"dinner\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"snacks\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}}}', 2),
(4, 4, '2026-04-27', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, '{\"breakfast\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"lunch\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"dinner\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"snacks\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}}}', 2),
(5, 5, '2026-04-28', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, '{\"breakfast\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"lunch\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"dinner\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"snacks\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}}}', 2),
(6, 6, '2026-04-29', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, '{\"breakfast\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"lunch\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"dinner\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"snacks\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}}}', 2),
(7, 7, '2026-04-30', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, '{\"breakfast\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"lunch\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"dinner\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}},\"snacks\":{\"calories\":0,\"protein\":0.0,\"carbs\":0.0,\"fats\":0.0,\"logged\":false,\"foodNames\":[],\"foods\":[],\"customFoods\":[],\"foodMacros\":{}}}', 2);

-- --------------------------------------------------------

--
-- Structure de la table `doctrine_migration_versions`
--

CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `doctrine_migration_versions`
--

INSERT INTO `doctrine_migration_versions` (`version`, `executed_at`, `execution_time`) VALUES
('DoctrineMigrations\\Version20260303235853', '2026-03-04 00:59:05', 1342);

-- --------------------------------------------------------

--
-- Structure de la table `evenement`
--

CREATE TABLE `evenement` (
  `id` int(11) NOT NULL,
  `nom` varchar(255) NOT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `lieu` varchar(255) NOT NULL,
  `statut` varchar(255) NOT NULL,
  `image` varchar(255) DEFAULT NULL,
  `description` longtext DEFAULT NULL,
  `coach_name` varchar(255) DEFAULT NULL,
  `objectifs` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `face_embeddings`
--

CREATE TABLE `face_embeddings` (
  `id` int(11) NOT NULL,
  `embedding_encrypted` longtext NOT NULL,
  `encryption_iv` varchar(255) NOT NULL,
  `encryption_tag` varchar(255) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `last_used_at` datetime DEFAULT NULL,
  `is_active` tinyint(4) NOT NULL,
  `consent_given_at` datetime NOT NULL,
  `consent_ip` varchar(45) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `face_verification_attempts`
--

CREATE TABLE `face_verification_attempts` (
  `id` int(11) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `ip_address` varchar(45) NOT NULL,
  `success` tinyint(4) NOT NULL,
  `similarity_score` double DEFAULT NULL,
  `attempted_at` datetime NOT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `gaspillage_log`
--

CREATE TABLE `gaspillage_log` (
  `id` int(11) NOT NULL,
  `ingredient_nom` varchar(150) NOT NULL,
  `quantite` decimal(10,2) NOT NULL,
  `unite` varchar(20) NOT NULL,
  `valeur_estimee` decimal(10,2) DEFAULT NULL,
  `date_jet` datetime NOT NULL,
  `etait_perime` tinyint(4) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `ingredient`
--

CREATE TABLE `ingredient` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `nom_en` varchar(100) DEFAULT NULL,
  `categorie` varchar(50) NOT NULL,
  `quantite` double NOT NULL,
  `unite` varchar(10) NOT NULL,
  `date_peremption` date DEFAULT NULL,
  `notes` longtext DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `ingredient`
--

INSERT INTO `ingredient` (`id`, `nom`, `nom_en`, `categorie`, `quantite`, `unite`, `date_peremption`, `notes`, `image`) VALUES
(1, 'Tomato', 'tomato', 'Vegetables', 502, 'g', '2026-03-15', 'Fresh tomatoes', 'https://images.unsplash.com/photo-1561136594-7f68813d8a5c?w=300'),
(2, 'Chicken Breast', 'chicken breast', 'Meats & Fish', 1, 'kg', '2026-02-28', 'Organic chicken', 'https://images.unsplash.com/photo-1598515214211-89d3c73ae83b?w=300'),
(3, 'Soy Sauce', 'soy sauce', 'Condiments', 2, 'ml', NULL, NULL, 'https://images.unsplash.com/photo-1569596082827-c3c11e7b3f6e?w=300'),
(4, 'Tomato', 'tomato', 'Vegetables', 10, 'g', NULL, NULL, 'https://images.unsplash.com/photo-1561136594-7f68813d8a5c?w=300'),
(7, 'Onion', 'onion', 'Vegetables', 0, 'g', NULL, NULL, 'https://images.unsplash.com/photo-1508747703725-719777637510?w=300'),
(8, 'Egg', 'egg', 'Dairy Products', 20, 'pcs', NULL, NULL, 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=300'),
(9, 'Beans', 'beans', 'Grocery Store', 0, 'g', NULL, NULL, 'https://images.unsplash.com/photo-1547592166-23ac45744acd?w=300'),
(10, 'Yogurt', 'yogurt', 'Dairy Products', 0, 'g', NULL, NULL, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=300'),
(15, 'Tomato Sauce', 'tomato sauce', 'Condiments', 151, 'kg', NULL, NULL, 'https://images.unsplash.com/photo-1546549032-9571cd6b27df?w=300'),
(16, 'Cheese', 'cheese', 'Dairy Products', 200, 'g', '2026-02-27', 'Contains allergens', 'https://images.unsplash.com/photo-1486297678162-eb2a19b0a318?w=300'),
(18, 'Ice Cream', 'ice cream', 'Frozen', 21, 'pcs', '2026-02-26', NULL, 'https://images.unsplash.com/photo-1576618148400-f54bed99fcfd?w=300'),
(21, 'Tteokbokki', 'tteokbokki', 'Grocery Store', 0, 'g', NULL, NULL, 'https://images.unsplash.com/photo-1586444248902-2f64eddc13df?w=300'),
(22, 'Egg', 'egg', 'Dairy Products', 0, 'g', NULL, NULL, 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=300'),
(25, 'Bread', 'bread', 'Grocery Store', 14, 'g', '2026-02-14', NULL, 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=300'),
(27, 'Plain Yogurt', 'plain yogurt', 'Dairy Products', 4, 'pcs', '2026-02-18', 'Opened 3 days ago', 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=300'),
(28, 'Raw Chicken', 'raw chicken', 'Meats & Fish', 500, 'g', '2026-02-18', 'Refrigerated', 'https://images.unsplash.com/photo-1598515214211-89d3c73ae83b?w=300'),
(29, 'Green Salad', 'green salad', 'Vegetables', 1, 'pcs', '2026-02-19', 'Starting to wilt', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=300'),
(32, 'Goat Cheese', 'goat cheese', 'Dairy Products', 150, 'g', '2026-02-15', 'Vacuum sealed', 'https://images.unsplash.com/photo-1559598467-f8b76c8155d0?w=300'),
(34, 'Smoked Salmon', 'smoked salmon', 'Meats & Fish', 100, 'g', '2026-02-15', 'Opened package', 'https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=300'),
(35, 'Cherry Tomatoes', 'cherry tomatoes', 'Vegetables', 500, 'g', '2026-02-20', 'Well ripened', 'https://images.unsplash.com/photo-1561136594-7f68813d8a5c?w=300'),
(37, 'Fresh Spinach', 'fresh spinach', 'Vegetables', 200, 'g', '2026-02-20', NULL, 'https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=300'),
(38, 'Sliced Bread', 'sliced bread', 'Grocery Store', 10, 'pcs', '2026-02-19', 'Opened 5 days ago', 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=300'),
(39, 'Orange Juice', 'orange juice', 'Drinks', 400, 'ml', '2026-02-14', 'Freshly squeezed', 'https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=300'),
(40, 'Ripe Avocado', 'ripe avocado', 'Fruits', 0, 'pcs', '2026-02-15', 'Perfectly ripe', 'https://images.unsplash.com/photo-1519162808019-7de1683fa2ad?w=300'),
(41, 'Fresh Basil', 'fresh basil', 'Vegetables', 1, 'pcs', '2026-02-15', 'In water', 'https://images.unsplash.com/photo-1618375532912-2c1e0dda0ef8?w=300'),
(42, 'Orange', 'orange', 'Fruits', 40, 'pcs', '2026-03-05', NULL, 'https://upload.wikimedia.org/wikipedia/commons/thumb/e/e3/Oranges_-_whole-halved-segment.jpg/960px-Oranges_-_whole-halved-segment.jpg'),
(43, 'Chocolate', 'chocolate', 'Grocery Store', 2, 'kg', '2026-03-01', NULL, 'https://images.unsplash.com/photo-1606914501449-5a96b6ce24ca?w=300'),
(44, 'Tomato', 'tomato', 'Vegetables', 50, 'pcs', '2026-03-15', NULL, 'https://images.unsplash.com/photo-1561136594-7f68813d8a5c?w=300'),
(45, 'Cheese', 'cheese', 'Dairy Products', 500, 'g', '2026-03-10', NULL, 'https://images.unsplash.com/photo-1486297678162-eb2a19b0a318?w=300'),
(46, 'Pasta', 'pasta', 'Grocery Store', 1000, 'g', '2026-06-01', NULL, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=300'),
(47, 'Chicken', 'chicken', 'Meats & Fish', 800, 'g', '2026-03-05', NULL, 'https://images.unsplash.com/photo-1598515214211-89d3c73ae83b?w=300'),
(48, 'Onion', 'onion', 'Vegetables', 30, 'pcs', '2026-04-01', NULL, 'https://images.unsplash.com/photo-1508747703725-719777637510?w=300'),
(49, 'Garlic', 'garlic', 'Vegetables', 20, 'pcs', '2026-04-15', NULL, 'https://images.unsplash.com/photo-1501430654243-c934cec2e1c0?w=300'),
(50, 'Milk', 'milk', 'Dairy Products', 2000, 'ml', '2026-03-08', NULL, 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=300'),
(51, 'Egg', 'egg', 'Dairy Products', 24, 'pcs', '2026-03-12', NULL, 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=300'),
(52, 'Flour', 'flour', 'Grocery Store', 2000, 'g', '2026-08-01', NULL, 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=300'),
(53, 'Butter', 'butter', 'Dairy Products', 500, 'g', '2026-03-20', NULL, 'https://images.unsplash.com/photo-1589985270958-a407b2fb7a16?w=300'),
(54, 'Chocolate', 'chocolate', 'Grocery Store', 494, 'g', '2026-08-01', NULL, 'https://images.unsplash.com/photo-1606914501449-5a96b6ce24ca?w=300'),
(55, 'Lemon', 'lemon', 'Fruits', 20, 'pcs', '2026-03-01', NULL, 'https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=300'),
(56, 'Mandarin', 'mandarin', 'Fruits', 25, 'pcs', '2026-03-03', NULL, 'https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=300'),
(57, 'Flour', 'flour', 'Grocery Store', 1000, 'g', '2026-06-01', NULL, 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?w=300'),
(58, 'Egg', 'egg', 'Dairy Products', 30, 'pcs', '2026-02-25', NULL, 'https://images.unsplash.com/photo-1618375532912-2c1e0dda0ef8?w=300'),
(59, 'Milk', 'milk', 'Dairy Products', 2, 'l', '2026-02-22', NULL, 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=300'),
(61, 'Beef', 'beef', 'Meats & Fish', 1, 'g', '2026-02-24', NULL, 'https://images.unsplash.com/photo-1588168333986-5078d3ae3976?w=300'),
(62, 'Egg', 'egg', 'Dairy Products', 0, 'g', '2026-02-26', NULL, 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=300'),
(63, 'Tofu', 'tofu', 'Dairy Products', 0, 'g', '2026-02-26', NULL, 'https://images.unsplash.com/photo-1545845512-1f3b71d06d6b?w=300'),
(64, 'Onion', 'onion', 'Vegetables', 2, 'g', '2026-02-27', NULL, 'https://images.unsplash.com/photo-1508747703725-719777637510?w=300'),
(66, 'Leek', 'leek', 'Vegetables', 2.5, 'g', '2026-02-25', NULL, 'https://images.unsplash.com/photo-1590165482129-1b8b27698780?w=300'),
(67, 'Beef', 'beef', 'Meats & Fish', 400, 'g', NULL, NULL, 'https://images.unsplash.com/photo-1588168333986-5078d3ae3976?w=300'),
(68, 'Egg', 'egg', 'Dairy Products', 1, 'pcs', NULL, NULL, 'https://images.unsplash.com/photo-1582722872445-44dc5f7e3c8f?w=300'),
(69, 'Beef Tallow', 'beef tallow', 'Meats & Fish', 100, 'g', NULL, NULL, 'https://images.unsplash.com/photo-1528825871115-3581a5387919?w=300'),
(70, 'Onion', 'onion', 'Vegetables', 1, 'pcs', NULL, NULL, 'https://images.unsplash.com/photo-1508747703725-719777637510?w=300'),
(71, 'Garlic', 'garlic', 'Condiments', 8, 'pcs', NULL, NULL, 'https://images.unsplash.com/photo-1501430654243-c934cec2e1c0?w=300'),
(72, 'Leek', 'leek', 'Vegetables', 0, 'pcs', NULL, NULL, 'https://images.unsplash.com/photo-1590165482129-1b8b27698780?w=300'),
(73, 'Banana', 'banana', 'Fruits', 1, 'kg', '2026-03-14', NULL, 'https://images.unsplash.com/photo-1603833665858-e61d17a86224?w=300'),
(83, 'Onion', 'onion', 'Vegetables', 10, 'g', '2026-04-17', NULL, 'https://images.unsplash.com/photo-1508747703725-719777637510?w=300'),
(84, 'Olive Oil', 'olive oil', 'Condiments', 0, 'ml', '2027-01-01', NULL, 'https://mag.guydemarle.com/app/uploads/2021/08/huile_olives.jpg'),
(85, 'Heavy Cream', 'heavy cream', 'Dairy Products', 0, 'ml', '2026-04-01', NULL, 'https://bojongourmet.com/wp-content/uploads/2023/09/2-ingredient-creme-fraiche.jpg'),
(86, 'Sugar', NULL, 'Grocery Store', 50, 'kg', '2027-06-01', '', 'https://t3.ftcdn.net/jpg/01/37/76/22/360_F_137762293_rRVXJZF2sNRNPQx0mUE6iFKGKU1Fn50U.jpg'),
(87, 'Rice', 'rice', 'Grocery Store', 0, 'g', '2027-06-01', NULL, 'https://www.mgc-prevention.fr/wp-content/uploads/2014/01/aliment-riz.jpg'),
(88, 'Potato', 'potato', 'Vegetables', 0, 'pcs', '2026-04-15', NULL, 'https://www.alimentarium.org/sites/default/files/media/image/2017-02/AL027-01_pomme_de_terre_0_0.jpg'),
(89, 'Bell Pepper', NULL, 'Vegetables', 70, 'pcs', '2026-04-10', '', 'https://feastforall.org/wp-content/uploads/2020/07/14880A.jpg'),
(90, 'Sunflower Oil', 'sunflower oil', 'Condiments', 500, 'ml', '2027-06-01', NULL, 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=300'),
(91, 'Coconut Oil', 'coconut oil', 'Condiments', 300, 'ml', '2027-06-01', NULL, 'https://images.unsplash.com/photo-1474979266404-7eaacbcd87c5?w=300'),
(92, 'Greek Yogurt', 'greek yogurt', 'Dairy Products', 300, 'g', '2026-04-10', NULL, 'https://images.unsplash.com/photo-1488477181946-6428a0291777?w=300'),
(93, 'watermelon', NULL, 'Grocery Store', 350, 'g', '2027-12-01', '', 'https://images.unsplash.com/photo-1587049352846-4a222e784d38?w=300'),
(94, 'Maple Syrup', 'maple syrup', 'Grocery Store', 175, 'ml', '2027-12-01', NULL, 'https://images.unsplash.com/photo-1589301760014-d929f3979dbc?w=300'),
(95, 'Whole Wheat Pasta', NULL, 'Grocery Store', 500, 'g', '2027-06-01', '', 'https://thumbs.dreamstime.com/b/organic-whole-wheat-pasta-durum-semolina-flour-macaroni-uncooked-penne-rigate-wooden-bowl-427670581.jpg'),
(96, 'Couscous', NULL, 'Grocery Store', 400, 'g', '2027-06-01', '', 'https://img.freepik.com/photos-gratuite/couscous-delicieux-dans-bol_1127-221.jpg'),
(97, 'Sweet Potato', NULL, 'Vegetables', 6, 'pcs', '2026-04-20', '', 'https://t3.ftcdn.net/jpg/02/95/67/48/360_F_295674852_DGl8KenKjNp15lI7WVEHIRhD5PVqYaGb.jpg'),
(98, 'Zucchini', 'zucchini', 'Vegetables', 4, 'pcs', '2026-04-15', NULL, 'https://images.unsplash.com/photo-1566486189376-d5f21e25aae4?w=300'),
(99, 'Margarine', NULL, 'Dairy Products', 250, 'g', '2026-05-01', '', 'https://thumbs.dreamstime.com/b/bo%C3%AEte-%C3%A0-margarine-102457123.jpg'),
(100, 'water', NULL, 'Frozen', 0, 'l', '2025-04-09', '', 'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=300');

-- --------------------------------------------------------

--
-- Structure de la table `liste_courses`
--

CREATE TABLE `liste_courses` (
  `id` int(11) NOT NULL,
  `nom_ingredient` varchar(100) NOT NULL,
  `quantite` double DEFAULT 0,
  `unite` varchar(20) DEFAULT NULL,
  `date_ajout` date NOT NULL,
  `est_achete` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `liste_courses`
--

INSERT INTO `liste_courses` (`id`, `nom_ingredient`, `quantite`, `unite`, `date_ajout`, `est_achete`) VALUES
(8, 'yaourt', 1, 'g', '2026-04-16', 0),
(10, 'Bell Pepper', 1, 'pcs', '2026-04-16', 0);

-- --------------------------------------------------------

--
-- Structure de la table `meal_plan`
--

CREATE TABLE `meal_plan` (
  `id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `date_creation` datetime DEFAULT current_timestamp(),
  `objectif` enum('PERTE_POIDS','MAINTIEN','PRISE_MASSE') DEFAULT NULL,
  `regime` enum('STANDARD','VEGETARIEN','VEGAN','HALAL') DEFAULT NULL,
  `allergie_lactose` tinyint(1) DEFAULT 0,
  `allergie_gluten` tinyint(1) DEFAULT 0,
  `allergie_nuts` tinyint(1) DEFAULT 0,
  `allergie_eggs` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `meal_plan`
--

INSERT INTO `meal_plan` (`id`, `user_id`, `date_creation`, `objectif`, `regime`, `allergie_lactose`, `allergie_gluten`, `allergie_nuts`, `allergie_eggs`) VALUES
(1, 1, '2026-04-22 22:09:08', 'MAINTIEN', 'VEGETARIEN', 1, 1, 0, 0),
(2, 11, '2026-04-24 21:42:17', 'PERTE_POIDS', 'STANDARD', 0, 0, 0, 1);

-- --------------------------------------------------------

--
-- Structure de la table `meal_plan_item`
--

CREATE TABLE `meal_plan_item` (
  `id` int(11) NOT NULL,
  `meal_plan_id` int(11) NOT NULL,
  `jour_nom` enum('Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi','Dimanche') DEFAULT NULL,
  `moment_repas` enum('PETIT_DEJEUNER','DEJEUNER','DINER') DEFAULT NULL,
  `recette_id` int(11) NOT NULL,
  `urgence_niveau` enum('URGENT','BIENTOT','OK') DEFAULT 'OK',
  `is_eaten` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `meal_plan_item`
--

INSERT INTO `meal_plan_item` (`id`, `meal_plan_id`, `jour_nom`, `moment_repas`, `recette_id`, `urgence_niveau`, `is_eaten`) VALUES
(1, 1, 'Lundi', 'PETIT_DEJEUNER', 12, 'OK', 1),
(2, 1, 'Lundi', 'DEJEUNER', 7, 'OK', 1),
(3, 1, 'Lundi', 'DINER', 9, 'OK', 1),
(4, 1, 'Mardi', 'PETIT_DEJEUNER', 12, 'OK', 0),
(5, 1, 'Mardi', 'DEJEUNER', 4, 'OK', 0),
(6, 1, 'Mardi', 'DINER', 16, 'OK', 0),
(7, 1, 'Mercredi', 'PETIT_DEJEUNER', 12, 'OK', 0),
(8, 1, 'Mercredi', 'DEJEUNER', 7, 'OK', 0),
(9, 1, 'Mercredi', 'DINER', 9, 'OK', 0),
(10, 1, 'Jeudi', 'PETIT_DEJEUNER', 12, 'OK', 0),
(11, 1, 'Jeudi', 'DEJEUNER', 7, 'OK', 0),
(12, 1, 'Jeudi', 'DINER', 9, 'OK', 0),
(13, 1, 'Vendredi', 'PETIT_DEJEUNER', 12, 'OK', 0),
(14, 1, 'Vendredi', 'DEJEUNER', 7, 'OK', 0),
(15, 1, 'Vendredi', 'DINER', 9, 'OK', 0),
(16, 1, 'Samedi', 'PETIT_DEJEUNER', 12, 'OK', 0),
(17, 1, 'Samedi', 'DEJEUNER', 7, 'OK', 0),
(18, 1, 'Samedi', 'DINER', 9, 'OK', 0),
(19, 1, 'Dimanche', 'PETIT_DEJEUNER', 12, 'OK', 0),
(20, 1, 'Dimanche', 'DEJEUNER', 7, 'OK', 0),
(21, 1, 'Dimanche', 'DINER', 9, 'OK', 0),
(22, 2, 'Lundi', 'PETIT_DEJEUNER', 327, 'OK', 1),
(23, 2, 'Lundi', 'DEJEUNER', 304, 'OK', 1),
(24, 2, 'Lundi', 'DINER', 16, 'URGENT', 1),
(25, 2, 'Mardi', 'PETIT_DEJEUNER', 313, 'OK', 0),
(26, 2, 'Mardi', 'DEJEUNER', 19, 'URGENT', 0),
(27, 2, 'Mardi', 'DINER', 307, 'OK', 0),
(28, 2, 'Mercredi', 'PETIT_DEJEUNER', 301, 'OK', 0),
(29, 2, 'Mercredi', 'DEJEUNER', 320, 'OK', 0),
(30, 2, 'Mercredi', 'DINER', 310, 'OK', 0),
(31, 2, 'Jeudi', 'PETIT_DEJEUNER', 309, 'OK', 0),
(32, 2, 'Jeudi', 'DEJEUNER', 300, 'OK', 0),
(33, 2, 'Jeudi', 'DINER', 315, 'OK', 0),
(34, 2, 'Vendredi', 'PETIT_DEJEUNER', 329, 'OK', 0),
(35, 2, 'Vendredi', 'DEJEUNER', 4, 'OK', 0),
(36, 2, 'Vendredi', 'DINER', 325, 'OK', 0),
(37, 2, 'Samedi', 'PETIT_DEJEUNER', 321, 'OK', 0),
(38, 2, 'Samedi', 'DEJEUNER', 308, 'OK', 0),
(39, 2, 'Samedi', 'DINER', 305, 'OK', 0),
(40, 2, 'Dimanche', 'PETIT_DEJEUNER', 316, 'OK', 0),
(41, 2, 'Dimanche', 'DEJEUNER', 312, 'OK', 0),
(42, 2, 'Dimanche', 'DINER', 322, 'OK', 0);

-- --------------------------------------------------------

--
-- Structure de la table `nutrition_objective`
--

CREATE TABLE `nutrition_objective` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `goal_type` varchar(50) DEFAULT NULL,
  `plan_level` varchar(30) DEFAULT NULL,
  `target_calories` int(11) NOT NULL,
  `target_protein` double NOT NULL,
  `target_carbs` double NOT NULL,
  `target_fats` double NOT NULL,
  `target_water` double DEFAULT NULL,
  `status` varchar(20) NOT NULL,
  `planned_start_date` date DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `updated_at` datetime DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `auto_activate` tinyint(4) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `nutrition_objective`
--

INSERT INTO `nutrition_objective` (`id`, `title`, `description`, `goal_type`, `plan_level`, `target_calories`, `target_protein`, `target_carbs`, `target_fats`, `target_water`, `status`, `planned_start_date`, `start_date`, `end_date`, `updated_at`, `created_at`, `auto_activate`, `user_id`) VALUES
(1, 'Gain Weight — Moderate', NULL, 'gain_weight', 'moderate', 2600, 180, 320, 80, 3, 'pending', '2026-04-25', NULL, NULL, '2026-04-24 12:43:57', '2026-04-24 12:43:57', 1, 11),
(2, 'Gain Weight — Moderate', NULL, 'gain_weight', 'moderate', 2600, 180, 320, 80, 3, 'active', '2026-04-25', '2026-04-23 23:00:00', '2026-04-29 23:00:00', '2026-04-24 12:48:26', '2026-04-24 12:48:20', 1, 12);

-- --------------------------------------------------------

--
-- Structure de la table `participation`
--

CREATE TABLE `participation` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `email` varchar(180) NOT NULL,
  `telephone` varchar(30) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `evenement_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `profile`
--

CREATE TABLE `profile` (
  `id` int(11) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `birthday` date NOT NULL,
  `weight` double NOT NULL,
  `height` double NOT NULL,
  `created_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `profile`
--

INSERT INTO `profile` (`id`, `first_name`, `last_name`, `birthday`, `weight`, `height`, `created_at`, `user_id`) VALUES
(1, 'Kodra', 'Fakraoui', '2005-03-31', 50, 140, '2026-02-25 23:53:49', 1),
(2, 'ahmed', 'sghaier', '2004-09-02', 75, 180, '2026-02-24 21:31:35', 2),
(4, 'kodra', 'fakraoui', '2004-01-07', 75, 180, '2026-02-25 21:42:59', 4),
(5, 'hamza', 'bezzin', '2005-06-25', 75, 180, '2026-02-25 21:43:47', 5),
(6, 'emna', 'belhsan', '2002-04-02', 75, 180, '2026-02-25 22:53:11', 6),
(7, 'emna', 'belhassen', '2002-04-02', 75, 180, '2026-02-25 23:03:05', 7),
(8, 'Salim', 'Sghaier', '2002-01-09', 70.5, 170, '2026-02-25 23:38:26', 8),
(9, 'maram', 'maram', '2004-03-31', 70, 170, '2026-03-04 01:08:28', 9);

-- --------------------------------------------------------

--
-- Structure de la table `progress_photo`
--

CREATE TABLE `progress_photo` (
  `id` int(11) NOT NULL,
  `photo_path` varchar(255) NOT NULL,
  `weight` decimal(5,2) DEFAULT NULL,
  `photo_type` varchar(50) NOT NULL,
  `notes` longtext DEFAULT NULL,
  `taken_at` datetime NOT NULL,
  `is_private` tinyint(4) NOT NULL DEFAULT 1,
  `admin_can_view` tinyint(4) NOT NULL DEFAULT 0,
  `created_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `reaction`
--

CREATE TABLE `reaction` (
  `id` int(11) NOT NULL,
  `type` varchar(20) NOT NULL,
  `created_at` datetime NOT NULL,
  `evenement_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `recette`
--

CREATE TABLE `recette` (
  `id` int(11) NOT NULL,
  `nom` varchar(150) NOT NULL,
  `type` varchar(20) NOT NULL,
  `difficulte` varchar(20) NOT NULL,
  `temps_preparation` int(11) NOT NULL,
  `description` longtext DEFAULT NULL,
  `image` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `etapes` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`etapes`)),
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `recette`
--

INSERT INTO `recette` (`id`, `nom`, `type`, `difficulte`, `temps_preparation`, `description`, `image`, `created_at`, `etapes`, `user_id`) VALUES
(1, 'salade cesar', 'dessert', 'easy', 15, 'umm', 'https://assets.afcdn.com/recipe/20190704/94706_w600.jpg', '2026-02-21 12:42:28', '[\"couper tomate en morceaux\"]', 11),
(4, 'salde mechouia', 'entree', 'facile', 45, 'le plat traditionnel tunisien', 'https://www.regimehypotoxique.com/wp-content/uploads/2016/07/SaladeGrillesTunisienne.jpg', '2026-02-21 12:42:28', '[\"couper les ingredients\",\"melanger tout et ajouter huile\"]', 4),
(5, 'jwajem', 'dessert', 'easy', 30, 'tres delicieuse', 'https://static.wixstatic.com/media/7302b9_80f53a41bcf94de8ae427a7abf6136b4~mv2.jpg/v1/fill/w_438,h_319,al_c,q_80,usm_0.66_1.00_0.01,enc_avif,quality_auto/7302b9_80f53a41bcf94de8ae427a7abf6136b4~mv2.jpg', '2026-02-21 12:42:28', '[\"couper les fraises\"]', 4),
(6, 'makarouna', 'plat', 'facile', 30, NULL, 'https://pngimg.com/uploads/spaghetti/spaghetti_PNG24.png', '2026-02-21 12:42:28', '[\"mets viande\"]', 4),
(7, 'takbouki', 'plat', 'moyen', 45, NULL, 'https://images.rawpixel.com/image_800/cHJpdmF0ZS9sci9pbWFnZXMvd2Vic2l0ZS8yMDIzLTExL3Jhd3BpeGVsX29mZmljZV8zMF9waG90b19vZl9rb3JlYW5fZm9vZF90dGVva2Jva2tpX29uX21pbmltYWxfcF80M2I5YTIxNC1mMmI2LTQ3NjctYjA2Ni03MTdjMTU4MWMxM2RfMS5qcGc.jpg', '2026-02-21 12:42:28', '[\"melanger tout\",\"ajouter ouef\"]', 4),
(9, 'leblebi', 'plat', 'facile', 20, NULL, 'https://ma-selection-naturelle.com/wp-content/uploads/2019/01/lablabi1-685x1024.png', '2026-02-21 12:42:28', '[\"lkhk\"]', 4),
(10, 'lazanya', 'main dish', 'hard', 55, NULL, 'https://iasbh.tmgrup.com.tr/580545/650/344/0/134/1143/738?u=http://i.sabah.com.tr/sbh/2016/12/22/lazanya-tarifi-lazanya-nasil-yapilir-1482408208138.png', '2026-02-21 12:42:28', '[\"Cook the meat\"]', 4),
(12, 'orange juice', 'drinks', 'easy', 5, NULL, 'https://storage.canalblog.com/43/82/392931/108577454_o.jpg', '2026-02-21 12:42:28', '[\"Wash the oranges\",\"Cut them in half\",\"Squeeze the juice using a juicer or by hand\"]', 11),
(13, 'pizza maison', 'main dish', 'medium', 60, 'pizza simple faite maison', 'https://img.over-blog-kiwi.com/1/02/97/00/20190413/ob_415ca6_8240994.JPG', '2026-02-21 12:42:28', '[\"preparer la pate\",\"ajouter sauce et fromage\",\"cuire au four\"]', 11),
(15, 'Cheese Omelette', 'main dish', 'easy', 10, 'Quick and fluffy cheese omelette', 'https://livesimply.me/wp-content/uploads/2022/04/cheese-omelette-recipe-ham-veggies-DSC09679-1024x1536.jpg', '2026-02-21 12:42:28', '[\"Beat the eggs\",\"Add cheese\",\"Cook in butter\"]', 11),
(16, 'Tomato Soup', 'entree', 'easy', 25, 'Classic homemade tomato soup', 'https://joyfoodsunshine.com/wp-content/uploads/2021/02/best-homemade-tomato-soup-recipe-1x1-1.jpg', '2026-02-21 12:42:28', '[\"Chop tomatoes and onions\",\"Cook in pot with garlic\",\"Blend until smooth\"]', 19),
(18, 'crepe', 'dessert', 'easy', 15, NULL, 'https://www.nordicware.com/wp-content/uploads/2021/05/classic_crepes_1.jpg', '2026-02-21 12:42:28', '[\"metter chocolat dans la pate\"]', 4),
(19, 'Pizza Margherita', 'main dish', 'medium', 45, 'Classic Italian pizza with tomato and cheese', 'https://assets.tmecosys.com/image/upload/t_web_rdp_recipe_584x480_1_5x/img/recipe/ras/Assets/5802fab5-fdce-468a-a830-43e8001f5a72/Derivates/c00dc34a-e73d-42f0-a86e-e2fd967d33fe.jpg', '2026-02-21 12:42:28', '[\"Make the dough\",\"Add tomato sauce\",\"Top with cheese\",\"Bake at 220C\"]', 19),
(20, 'Chocolate Pancakes', 'dessert', 'facile', 20, 'pancakes au chocolat', 'https://images.ricardocuisine.com/services/recipes/992x1340_pancakes.jpg', '2026-02-21 12:42:28', '[\"Melanger farine lait oeufs\",\"Ajouter chocolat\",\"Cuire a la poele\"]', 19),
(21, 'Chocolate Waffles', 'dessert', 'moyen', 25, 'gaufres chocolat', 'https://www.everydayeileen.com/wp-content/uploads/2025/02/chocolate-waffles-1.jpg', '2026-02-21 12:42:28', '[\"Preparer pate\",\"Verser dans gaufrier\",\"Cuire\"]', 19),
(22, 'Brownies', 'dessert', 'medium', 35, 'gateau chocolat', 'https://icecreambakery.in/wp-content/uploads/2024/12/Brownie-Recipe-with-Cocoa-Powder.jpg', '2026-02-21 12:42:28', '[\"ajouter tomate concentre\"]', 19),
(24, 'tajine', 'main dish', 'easy', 45, '', '', '2026-04-24 11:46:39', '[\"hi\"]', 7),
(200, 'Avocado Toast', 'main dish', 'easy', 10, 'Healthy avocado on toast', 'https://cdn.loveandlemons.com/wp-content/uploads/2020/01/avocado-toast-500x375.jpghttps://cdn.loveandlemons.com/wp-content/uploads/2020/01/avocado-toast-500x375.jpg', '2026-04-24 18:46:06', '[\"Toast bread\",\"Mash avocado\",\"Season and serve\"]', 11),
(201, 'Greek Yogurt Bowl', 'dessert', 'easy', 5, 'Yogurt with fruits and granola', 'https://plaineverything.com/wp-content/uploads/2024/08/Greek-Yogurt-Bowl-1024x1024.jpghttps://plaineverything.com/wp-content/uploads/2024/08/Greek-Yogurt-Bowl-1024x1024.jpg', '2026-04-24 18:46:06', '[\"Add yogurt\",\"Top with fruits\",\"Add granola\"]', 11),
(202, 'Banana Smoothie', 'drinks', 'easy', 5, 'Creamy banana smoothie', 'https://t3.ftcdn.net/jpg/06/80/29/28/360_F_680292854_LErIPEtJpJSuog1MIER8zk8KAD1VHsoD.jpg', '2026-04-24 18:46:06', '[\"Blend banana\",\"Add milk\",\"Blend until smooth\"]', 11),
(203, 'Scrambled Eggs', 'main dish', 'easy', 8, 'Fluffy scrambled eggs', 'https://www.shutterstock.com/image-photo/scrambled-eggs-on-plate-260nw-171828170.jpg', '2026-04-24 18:46:06', '[\"Beat eggs\",\"Cook in butter\",\"Season\"]', 11),
(204, 'Chicken Caesar Salad', 'main dish', 'medium', 20, 'Classic Caesar with grilled chicken', 'https://www.erinliveswhole.com/wp-content/uploads/2021/03/chicken-caesar-salad-6.jpg', '2026-04-24 18:46:06', '[\"Grill chicken\",\"Prepare lettuce\",\"Add dressing\"]', 11),
(205, 'Lentil Soup', 'entree', 'easy', 30, 'Hearty lentil soup', 'https://itsnotcomplicatedrecipes.com/wp-content/uploads/2023/09/Italian-Lentil-Soup-Feature.jpg', '2026-04-24 18:46:06', '[\"Saute onion\",\"Add lentils\",\"Simmer 25min\"]', 11),
(206, 'Grilled Salmon', 'main dish', 'medium', 20, 'Healthy grilled salmon fillet', 'https://www.dinneratthezoo.com/wp-content/uploads/2019/05/grilled-salmon-final-2.jpg', '2026-04-24 18:46:06', '[\"Season salmon\",\"Grill 10min each side\",\"Serve\"]', 11),
(207, 'Vegetable Stir Fry', 'main dish', 'easy', 15, 'Quick veggie stir fry', 'https://kristineskitchenblog.com/wp-content/uploads/2024/01/vegetable-stir-fry-22-3.jpg', '2026-04-24 18:46:06', '[\"Chop vegetables\",\"Stir fry in oil\",\"Add sauce\"]', 11),
(208, 'Hummus Bowl', 'entree', 'easy', 10, 'Creamy hummus with veggies', 'https://www.attainable-sustainable.net/wp-content/uploads/2022/12/hummus-bowl-3.jpg', '2026-04-24 18:46:06', '[\"Spread hummus\",\"Add veggies\",\"Drizzle olive oil\"]', 11),
(209, 'Berry Smoothie Bowl', 'dessert', 'easy', 10, 'Thick smoothie bowl with berries', 'https://www.mydarlingvegan.com/wp-content/uploads/2017/01/Smoothie6.jpg', '2026-04-24 18:46:06', '[\"Blend frozen berries\",\"Pour in bowl\",\"Add toppings\"]', 11),
(210, 'Tuna Pasta', 'main dish', 'easy', 20, 'Simple tuna pasta', 'https://itsnotcomplicatedrecipes.com/wp-content/uploads/2024/07/Spicy-Tuna-and-Tomato-Pasta-Feature.jpg', '2026-04-24 18:46:06', '[\"Cook pasta\",\"Mix with tuna\",\"Season\"]', 11),
(211, 'Mushroom Risotto', 'main dish', 'hard', 40, 'Creamy mushroom risotto', 'https://www.recipetineats.com/uploads/2019/10/Mushroom-Risotto_7.jpg', '2026-04-24 18:46:06', '[\"Saute mushrooms\",\"Add rice\",\"Add stock gradually\"]', 11),
(212, 'Veggie Burger', 'main dish', 'medium', 25, 'Homemade veggie burger', 'https://t4.ftcdn.net/jpg/15/95/54/57/360_F_1595545758_V0ppMdXPKxyQvS32uFk99FJknMCj7sui.jpg', '2026-04-24 18:46:06', '[\"Make patty\",\"Grill\",\"Assemble\"]', 11),
(213, 'Minestrone Soup', 'entree', 'easy', 35, 'Italian vegetable soup', 'https://cdn.loveandlemons.com/wp-content/uploads/2021/11/minestrone-soup-500x500.jpg', '2026-04-24 18:46:06', '[\"Saute vegetables\",\"Add stock\",\"Simmer 30min\"]', 11),
(214, 'Overnight Oats', 'main dish', 'easy', 5, 'No-cook oats for breakfast', 'https://cdn.apartmenttherapy.info/image/upload/v1739991687/k/Photo/Recipes/2025-02-overnight-oats/overnight-oats-484.jpg', '2026-04-24 18:46:06', '[\"Mix oats with milk\",\"Add toppings\",\"Refrigerate\"]', 11),
(215, 'French Toast', 'dessert', 'easy', 15, 'Classic French toast', 'https://altonbrown.com/wp-content/uploads/2020/08/French-Toast-Stack_Lynne_resized.jpg', '2026-04-24 18:46:06', '[\"Dip bread in egg\",\"Cook in butter\",\"Serve with syrup\"]', 11),
(216, 'Chickpea Curry', 'main dish', 'medium', 30, 'Spicy chickpea curry', 'https://images.immediate.co.uk/production/volatile/sites/30/2020/08/kadala-curry-fad4df3.jpg', '2026-04-24 18:46:06', '[\"Saute onion\",\"Add spices\",\"Add chickpeas and simmer\"]', 11),
(217, 'Baked Sweet Potato', 'main dish', 'easy', 45, 'Simple baked sweet potato', 'https://static01.nyt.com/images/2023/07/10/multimedia/MS-Baked-Sweet-Potatoes-kcmq/MS-Baked-Sweet-Potatoes-kcmq-jumbo.jpg', '2026-04-24 18:46:06', '[\"Prick potato\",\"Bake 45min\",\"Add toppings\"]', 11),
(218, 'Green Detox Juice', 'drinks', 'easy', 5, 'Healthy green juice', 'https://www.inspiredtaste.net/wp-content/uploads/2025/11/Green-Juice-Recipe-1.jpg', '2026-04-24 18:46:06', '[\"Juice cucumber\",\"Add spinach\",\"Add apple\"]', 11),
(219, 'Fruit Salad', 'dessert', 'easy', 10, 'Fresh seasonal fruit salad', 'https://cdn.loveandlemons.com/wp-content/uploads/2025/06/fruit-salad.jpg', '2026-04-24 18:46:06', '[\"Cut fruits\",\"Mix together\",\"Add mint\"]', 11),
(220, 'Shakshuka', 'main dish', 'medium', 25, 'Eggs in tomato sauce', 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/18/Shakshuka_by_Calliopejen1.jpg/1280px-Shakshuka_by_Calliopejen1.jpg', '2026-04-24 18:46:06', '[\"Make tomato sauce\",\"Add eggs\",\"Cover and cook\"]', 11),
(221, 'Quinoa Bowl', 'main dish', 'easy', 20, 'Nutritious quinoa bowl', 'https://cdn.aboutamom.com/uploads/2023/06/Easy-Quinoa-Bowl-Recipe-Feature-Photo-4-F-scaled.jpg', '2026-04-24 18:46:06', '[\"Cook quinoa\",\"Add veggies\",\"Drizzle dressing\"]', 11),
(222, 'Pumpkin Soup', 'entree', 'easy', 30, 'Creamy pumpkin soup', 'https://www.healthyfood.com/wp-content/uploads/2019/07/Creamy-pumpkin-soup.jpg', '2026-04-24 18:46:06', '[\"Roast pumpkin\",\"Blend\",\"Season\"]', 11),
(223, 'Egg Muffins', 'main dish', 'easy', 20, 'Protein-packed egg muffins', 'https://t4.ftcdn.net/jpg/01/22/47/53/360_F_122475300_0fOpGYLnyOqvFrbLcPgI8gQKHtqzKvMB.jpg', '2026-04-24 18:46:06', '[\"Beat eggs\",\"Add veggies\",\"Bake 18min\"]', 11),
(224, 'Mango Lassi', 'drinks', 'easy', 5, 'Indian mango yogurt drink', 'https://lentillovingfamily.com/wp-content/uploads/2025/05/mango-lassi-2.jpg', '2026-04-24 18:46:06', '[\"Blend mango\",\"Add yogurt\",\"Add sugar\"]', 11),
(225, 'Pasta Primavera', 'main dish', 'medium', 25, 'Pasta with spring vegetables', 'https://www.budgetbytes.com/wp-content/uploads/2023/05/Pasta-Primavera-fork.jpg', '2026-04-24 18:46:06', '[\"Cook pasta\",\"Saute veggies\",\"Combine\"]', 11),
(226, 'Tabbouleh', 'entree', 'easy', 15, 'Lebanese parsley salad', 'https://thegreekfoodie.com/wp-content/uploads/2024/02/Tabouli_Salad_Tabbouleh_SQ.jpg', '2026-04-24 18:46:06', '[\"Chop parsley\",\"Add bulgur\",\"Season with lemon\"]', 11),
(227, 'Apple Cinnamon Oatmeal', 'main dish', 'easy', 10, 'Warm oatmeal with apple', 'https://www.eatwell101.com/wp-content/uploads/2020/09/Apple-Cinnamon-Oatmeal.jpg', '2026-04-24 18:46:06', '[\"Cook oats\",\"Add apple\",\"Sprinkle cinnamon\"]', 11),
(228, 'Gazpacho', 'entree', 'easy', 10, 'Cold Spanish tomato soup', 'https://www.greenvillage.ma/vyckungy/2024/07/recettes-de-gaspacho-1-e1656499198983.jpg', '2026-04-24 18:46:06', '[\"Blend tomatoes\",\"Add cucumber\",\"Chill\"]', 11),
(229, 'Spinach Frittata', 'main dish', 'medium', 20, 'Italian egg frittata', 'https://therecipewell.com/wp-content/uploads/2020/01/Potato-and-Spinach-Frittata-square.jpg', '2026-04-24 18:46:06', '[\"Saute spinach\",\"Add eggs\",\"Bake 15min\"]', 11),
(300, 'Baked Chicken Thighs', 'main dish', 'easy', 35, 'Juicy oven baked chicken', 'https://www.spoonforkbacon.com/wp-content/uploads/2020/09/oven-baked-chicken-thighs-recipe-card.jpg', '2026-04-24 18:46:06', '[\"Season chicken\",\"Bake 35min\",\"Rest 5min\"]', 19),
(301, 'Acai Bowl', 'dessert', 'easy', 10, 'Thick acai smoothie bowl', 'https://static.vecteezy.com/system/resources/previews/054/720/932/non_2x/delicious-acai-bowls-with-fruits-on-transparent-background-png.png', '2026-04-24 18:46:06', '[\"Blend frozen acai\",\"Pour in bowl\",\"Add toppings\"]', 19),
(302, 'Protein Shake', 'drinks', 'easy', 5, 'Post-workout protein shake', 'https://png.pngtree.com/png-vector/20250206/ourmid/pngtree-healthy-fruit-smoothie-with-strawberries-bananas-and-almonds-png-image_15310493.png', '2026-04-24 18:46:06', '[\"Add protein powder\",\"Add milk\",\"Blend\"]', 19),
(303, 'Poached Eggs', 'main dish', 'medium', 10, 'Perfect poached eggs', 'https://t3.ftcdn.net/jpg/06/18/83/34/360_F_618833498_PfgXio7w4orO8K9vMiaQyUz10ZOleotA.jpg', '2026-04-24 18:46:06', '[\"Boil water\",\"Add vinegar\",\"Poach eggs 3min\"]', 19),
(304, 'Beef Stir Fry', 'main dish', 'medium', 20, 'Asian style beef stir fry', 'https://static.vecteezy.com/system/resources/previews/055/209/228/non_2x/bowl-of-stir-fried-beef-and-vegetables-free-png.png', '2026-04-24 18:46:06', '[\"Slice beef\",\"Stir fry\",\"Add sauce\"]', 19),
(305, 'French Onion Soup', 'entree', 'hard', 45, 'Classic French onion soup', 'https://img.freepik.com/premium-psd/french-onion-soup-isolated-transparent-background_191095-44785.jpg', '2026-04-24 18:46:06', '[\"Caramelize onions\",\"Add broth\",\"Top with cheese\"]', 19),
(306, 'Tuna Nicoise Salad', 'main dish', 'easy', 15, 'French Nicoise with tuna', 'https://png.pngtree.com/png-clipart/20240921/original/pngtree-nicoise-salad-salad-appetizer-salad-nicoise-photo-png-image_16058825.png', '2026-04-24 18:46:06', '[\"Boil eggs\",\"Add tuna\",\"Arrange salad\"]', 19),
(307, 'Stuffed Peppers', 'main dish', 'medium', 40, 'Peppers stuffed with rice', 'https://t3.ftcdn.net/jpg/01/71/94/78/360_F_171947893_igJJruy5RRFvWippKtp7s5r346R13Tob.jpg', '2026-04-24 18:46:06', '[\"Hollow peppers\",\"Fill with rice\",\"Bake 35min\"]', 19),
(308, 'Tzatziki', 'entree', 'easy', 10, 'Greek yogurt dip', 'https://e7.pngegg.com/pngimages/778/883/png-clipart-tzatziki-greek-cuisine-gyro-raita-pita-yogurt-food-recipe.png', '2026-04-24 18:46:06', '[\"Grate cucumber\",\"Mix with yogurt\",\"Add garlic\"]', 19),
(309, 'Chia Pudding', 'dessert', 'easy', 5, 'Overnight chia seed pudding', 'https://thumbs.dreamstime.com/b/chia-pudding-fruits-isolated-transparent-background-chia-pudding-fruits-isolated-transparent-white-background-288677119.jpg', '2026-04-24 18:46:06', '[\"Mix chia with milk\",\"Add honey\",\"Refrigerate\"]', 19),
(310, 'Lamb Tagine', 'main dish', 'hard', 90, 'Moroccan lamb tagine', 'https://png.pngtree.com/background/20250602/original/pngtree-traditional-moroccan-tagine-with-lamb-and-vegetables-picture-image_16607742.jpg', '2026-04-24 18:46:06', '[\"Brown lamb\",\"Add spices\",\"Slow cook 80min\"]', 19),
(311, 'Vegetable Lasagna', 'main dish', 'hard', 60, 'Layered veggie lasagna', 'https://girlheartfood.com/wp-content/uploads/2019/10/Veggie-Lasagna-Recipe-Feature-500x375.jpg', '2026-04-24 18:46:06', '[\"Make sauce\",\"Layer pasta\",\"Bake 45min\"]', 19),
(312, 'Bruschetta', 'entree', 'easy', 10, 'Italian tomato bruschetta', 'https://www.shutterstock.com/image-photo/bruschetta-tomato-basil-olive-oil-600nw-2683008653.jpg', '2026-04-24 18:46:06', '[\"Toast bread\",\"Top with tomato\",\"Add basil\"]', 19),
(313, 'Panna Cotta', 'dessert', 'medium', 20, 'Italian cream dessert', 'https://upload.wikimedia.org/wikipedia/commons/thumb/0/01/Panna_cotta_with_chocolate_mousse.jpg/330px-Panna_cotta_with_chocolate_mousse.jpg', '2026-04-24 18:46:06', '[\"Heat cream\",\"Add gelatin\",\"Chill\"]', 19),
(314, 'Egg White Omelette', 'main dish', 'easy', 10, 'Light egg white omelette', 'https://www.simplyquinoa.com/wp-content/uploads/2023/03/egg-white-omelet-1.jpg', '2026-04-24 18:46:06', '[\"Separate eggs\",\"Beat whites\",\"Cook in pan\"]', 19),
(315, 'Tomato Basil Pasta', 'main dish', 'easy', 20, 'Simple tomato pasta', 'https://frommybowl.com/wp-content/uploads/2022/07/Spicy_Tomato_Basil_Pasta_Vegan_FromMyBowl-12.jpg', '2026-04-24 18:46:06', '[\"Cook pasta\",\"Make sauce\",\"Combine\"]', 19),
(316, 'Herbal Tea', 'drinks', 'easy', 5, 'Relaxing herbal tea blend', 'https://www.botanicalinterests.com/community/blog/wp-content/uploads/2024/08/herbs-for-tea.jpg', '2026-04-24 18:46:06', '[\"Boil water\",\"Add herbs\",\"Steep 5min\"]', 19),
(317, 'Caesar Dip', 'entree', 'easy', 5, 'Quick Caesar dressing dip', 'https://thekittchen.com/wp-content/uploads/2019/10/Caesar-Dip-2.jpg', '2026-04-24 18:46:06', '[\"Mix all ingredients\",\"Chill\",\"Serve\"]', 19),
(318, 'Lemon Tart', 'dessert', 'hard', 60, 'Classic French lemon tart', 'https://www.recipetineats.com/tachyon/2021/06/French-Lemon-Tart_5-main-SQ.jpg', '2026-04-24 18:46:06', '[\"Make pastry\",\"Make lemon curd\",\"Bake\"]', 19),
(319, 'Stuffed Mushrooms', 'entree', 'medium', 25, 'Mushrooms stuffed with cheese', 'https://www.feastingathome.com/wp-content/uploads/2025/11/Stuffed-Mushrooms-21.jpg', '2026-04-24 18:46:06', '[\"Remove stems\",\"Stuff with cheese\",\"Bake 20min\"]', 19),
(320, 'Beef Tacos', 'main dish', 'medium', 25, 'Mexican style beef tacos', 'https://t3.ftcdn.net/jpg/01/21/83/80/360_F_121838061_eb9T4qBv5fVsRQXTjQLYnbHSAJejpwcK.jpg', '2026-04-24 18:46:06', '[\"Cook beef\",\"Season\",\"Fill tacos\"]', 19),
(321, 'Watermelon Juice', 'drinks', 'easy', 5, 'Fresh watermelon juice', 'https://www.rebootwithjoe.com/wp-content/uploads/2012/05/watermelon-pineapple-juice.jpg', '2026-04-24 18:46:06', '[\"Blend watermelon\",\"Strain\",\"Chill\"]', 19),
(322, 'Borscht', 'entree', 'hard', 50, 'Russian beetroot soup', 'https://cdn.apartmenttherapy.info/image/upload/v1697655004/k/Photo/Recipes/2023-10-borscht/borscht-035.jpg', '2026-04-24 18:46:06', '[\"Cook beets\",\"Add vegetables\",\"Simmer 40min\"]', 19),
(323, 'Tiramisu', 'dessert', 'hard', 30, 'Italian coffee dessert', 'https://www.shutterstock.com/image-photo/italian-dessert-tiramisu-made-ladyfingers-260nw-2554215847.jpg', '2026-04-24 18:46:06', '[\"Make cream\",\"Dip ladyfingers\",\"Layer\"]', 19),
(324, 'Shakshuka Verde', 'main dish', 'medium', 25, 'Green shakshuka variation', 'https://marleyspoon.com/media/recipes/188847/main_photos/large/shakshuka-c12a877ca60e4a781b441d7fb9e4bc50.jpeg', '2026-04-24 18:46:06', '[\"Make green sauce\",\"Add eggs\",\"Cover and cook\"]', 19),
(325, 'Coconut Rice', 'main dish', 'easy', 25, 'Thai coconut rice', 'https://www.indianhealthyrecipes.com/wp-content/uploads/2021/05/coconut-rice-recipe.jpg', '2026-04-24 18:46:06', '[\"Rinse rice\",\"Cook in coconut milk\",\"Season\"]', 19),
(326, 'Miso Soup', 'entree', 'easy', 10, 'Japanese miso soup', 'https://www.justonecookbook.com/wp-content/uploads/2022/06/Miso-Soup-8297-I.jpg', '2026-04-24 18:46:06', '[\"Heat dashi\",\"Add miso\",\"Add tofu\"]', 19),
(327, 'Croissant', 'main dish', 'easy', 5, 'Buttery French croissant', 'https://t4.ftcdn.net/jpg/03/66/13/57/360_F_366135713_iSnaXZVccWVn8xcqnpGnkQTrnwSWLtfu.jpg', '2026-04-24 18:46:06', '[\"Warm croissant\",\"Serve with jam\"]', 19),
(328, 'Detox Water', 'drinks', 'easy', 3, 'Infused detox water', 'https://media.post.rvohealth.io/wp-content/uploads/2020/09/PVPV163_Detox-Water-Benefits-Myths_732x549_thumb-1-732x549.jpg', '2026-04-24 18:46:06', '[\"Add fruits to water\",\"Chill overnight\",\"Serve\"]', 19),
(329, 'Baked Oatmeal', 'main dish', 'easy', 30, 'Oven baked oatmeal', 'https://static01.nyt.com/images/2023/12/14/multimedia/LH-Baked-Oatmeal-zjbc/LH-Baked-Oatmeal-zjbc-videoSixteenByNineJumbo1600.jpg', '2026-04-24 18:46:06', '[\"Mix oats\",\"Bake 25min\",\"Serve warm\"]', 19);

-- --------------------------------------------------------

--
-- Structure de la table `recette_favoris`
--

CREATE TABLE `recette_favoris` (
  `id` int(11) NOT NULL,
  `added_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `recette_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `recette_favoris`
--

INSERT INTO `recette_favoris` (`id`, `added_at`, `user_id`, `recette_id`) VALUES
(2, '2026-04-23 23:13:28', 1, 4),
(3, '2026-04-20 10:00:00', 11, 5),
(4, '2026-04-20 10:01:00', 11, 18),
(5, '2026-04-20 10:02:00', 11, 15),
(6, '2026-04-20 10:03:00', 11, 12),
(8, '2026-04-20 11:01:00', 19, 10),
(9, '2026-04-20 11:02:00', 19, 16),
(10, '2026-04-20 11:03:00', 19, 4),
(11, '2026-04-20 12:00:00', 12, 1),
(12, '2026-04-20 12:01:00', 12, 7),
(13, '2026-04-20 12:02:00', 12, 13),
(14, '2026-04-20 12:03:00', 12, 22),
(15, '2026-04-24 18:10:13', 19, 19),
(16, '2026-04-24 22:25:53', 19, 305),
(17, '2026-04-24 22:36:53', 11, 202),
(18, '2026-04-24 22:36:57', 11, 201);

-- --------------------------------------------------------

--
-- Structure de la table `recette_info_plus`
--

CREATE TABLE `recette_info_plus` (
  `recette_id` int(11) NOT NULL,
  `calories` int(11) DEFAULT NULL,
  `proteines` float DEFAULT NULL,
  `lipides` float DEFAULT NULL,
  `glucides` float DEFAULT NULL,
  `moment_repas` enum('PETIT_DEJEUNER','DEJEUNER','DINER') DEFAULT NULL,
  `is_vegetarien` tinyint(1) DEFAULT 0,
  `is_vegan` tinyint(1) DEFAULT 0,
  `is_halal` tinyint(1) DEFAULT 0,
  `contains_gluten` tinyint(1) DEFAULT 0,
  `contains_lactose` tinyint(1) DEFAULT 0,
  `contains_nuts` tinyint(1) DEFAULT 0,
  `contains_eggs` tinyint(1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `recette_info_plus`
--

INSERT INTO `recette_info_plus` (`recette_id`, `calories`, `proteines`, `lipides`, `glucides`, `moment_repas`, `is_vegetarien`, `is_vegan`, `is_halal`, `contains_gluten`, `contains_lactose`, `contains_nuts`, `contains_eggs`) VALUES
(1, 320, 12, 18, 25, 'DEJEUNER', 1, 0, 1, 1, 1, 0, 1),
(4, 180, 4, 9, 20, 'DEJEUNER', 1, 1, 1, 0, 0, 0, 0),
(5, 250, 15, 8, 28, 'DINER', 0, 0, 1, 0, 0, 0, 1),
(6, 480, 14, 10, 72, 'DEJEUNER', 1, 0, 1, 1, 1, 0, 0),
(7, 350, 10, 7, 58, 'DEJEUNER', 1, 1, 1, 0, 0, 0, 1),
(9, 290, 12, 6, 42, 'DINER', 1, 1, 1, 0, 0, 0, 0),
(10, 520, 22, 20, 58, 'DINER', 0, 0, 0, 1, 1, 0, 1),
(12, 90, 1.5, 0.2, 21, 'PETIT_DEJEUNER', 1, 1, 1, 0, 0, 0, 0),
(13, 580, 20, 22, 70, 'DEJEUNER', 1, 0, 0, 1, 1, 0, 0),
(15, 310, 18, 22, 4, 'PETIT_DEJEUNER', 1, 0, 1, 0, 1, 0, 1),
(16, 160, 4, 5, 22, 'DINER', 1, 1, 1, 0, 0, 0, 0),
(18, 280, 7, 9, 40, 'PETIT_DEJEUNER', 1, 0, 1, 1, 1, 0, 1),
(19, 540, 18, 19, 68, 'DEJEUNER', 1, 0, 0, 1, 1, 0, 0),
(20, 420, 9, 16, 60, 'PETIT_DEJEUNER', 1, 0, 1, 1, 1, 0, 1),
(21, 450, 8, 18, 62, 'PETIT_DEJEUNER', 1, 0, 1, 1, 1, 0, 1),
(22, 380, 5, 18, 52, 'PETIT_DEJEUNER', 1, 0, 1, 1, 1, 0, 1),
(200, 310, 8, 14, 32, 'PETIT_DEJEUNER', 1, 1, 1, 1, 0, 0, 0),
(201, 220, 12, 5, 30, 'PETIT_DEJEUNER', 1, 0, 1, 0, 1, 1, 0),
(202, 180, 3, 1, 38, 'PETIT_DEJEUNER', 1, 1, 1, 0, 0, 0, 0),
(203, 280, 18, 20, 2, 'PETIT_DEJEUNER', 1, 0, 1, 0, 0, 0, 1),
(204, 420, 38, 18, 12, 'DEJEUNER', 0, 0, 1, 1, 1, 0, 0),
(205, 210, 14, 4, 32, 'DEJEUNER', 1, 1, 1, 0, 0, 0, 0),
(206, 380, 42, 18, 0, 'DEJEUNER', 0, 0, 1, 0, 0, 0, 0),
(207, 290, 8, 10, 38, 'DEJEUNER', 1, 1, 1, 0, 0, 0, 0),
(208, 240, 10, 12, 24, 'DEJEUNER', 1, 1, 1, 0, 0, 1, 0),
(209, 280, 6, 8, 44, 'PETIT_DEJEUNER', 1, 1, 1, 0, 0, 1, 0),
(210, 440, 28, 12, 54, 'DINER', 0, 0, 1, 1, 0, 0, 0),
(211, 480, 14, 16, 66, 'DINER', 1, 0, 1, 0, 1, 0, 0),
(212, 380, 18, 14, 44, 'DEJEUNER', 1, 1, 1, 1, 0, 0, 0),
(213, 190, 8, 4, 28, 'DINER', 1, 1, 1, 1, 0, 0, 0),
(214, 310, 10, 6, 52, 'PETIT_DEJEUNER', 1, 0, 1, 1, 1, 1, 0),
(215, 340, 10, 14, 44, 'PETIT_DEJEUNER', 1, 0, 1, 1, 1, 0, 1),
(216, 380, 16, 12, 52, 'DINER', 1, 1, 1, 0, 0, 0, 0),
(217, 290, 5, 6, 54, 'DINER', 1, 1, 1, 0, 0, 0, 0),
(218, 90, 3, 1, 18, 'PETIT_DEJEUNER', 1, 1, 1, 0, 0, 0, 0),
(219, 140, 2, 1, 32, 'PETIT_DEJEUNER', 1, 1, 1, 0, 0, 0, 0),
(220, 310, 18, 14, 24, 'PETIT_DEJEUNER', 1, 1, 1, 0, 0, 0, 1),
(221, 350, 12, 10, 48, 'DEJEUNER', 1, 1, 1, 0, 0, 1, 0),
(222, 180, 4, 8, 22, 'DINER', 1, 1, 1, 0, 0, 0, 0),
(223, 260, 20, 16, 6, 'PETIT_DEJEUNER', 1, 0, 1, 0, 0, 0, 1),
(224, 200, 6, 4, 36, 'PETIT_DEJEUNER', 1, 0, 1, 0, 1, 0, 0),
(225, 420, 14, 12, 62, 'DINER', 1, 0, 1, 1, 1, 0, 0),
(226, 160, 5, 4, 24, 'DEJEUNER', 1, 1, 1, 0, 0, 0, 0),
(227, 290, 8, 6, 52, 'PETIT_DEJEUNER', 1, 1, 1, 1, 0, 1, 0),
(228, 120, 3, 4, 18, 'DINER', 1, 1, 1, 0, 0, 0, 0),
(229, 320, 22, 18, 8, 'DEJEUNER', 1, 0, 1, 0, 0, 0, 1),
(300, 420, 38, 22, 6, 'DEJEUNER', 0, 0, 1, 0, 0, 0, 0),
(301, 260, 6, 10, 38, 'PETIT_DEJEUNER', 1, 1, 1, 0, 0, 1, 0),
(302, 220, 30, 4, 18, 'PETIT_DEJEUNER', 0, 0, 1, 0, 1, 0, 0),
(303, 160, 14, 10, 2, 'PETIT_DEJEUNER', 1, 0, 1, 0, 0, 0, 1),
(304, 440, 36, 22, 18, 'DEJEUNER', 0, 0, 1, 0, 0, 0, 0),
(305, 280, 10, 12, 28, 'DINER', 1, 0, 1, 1, 1, 0, 0),
(306, 320, 28, 14, 14, 'DEJEUNER', 0, 0, 1, 0, 0, 0, 1),
(307, 380, 16, 12, 48, 'DINER', 1, 0, 1, 0, 0, 0, 0),
(308, 120, 6, 4, 12, 'DEJEUNER', 1, 0, 1, 0, 1, 0, 0),
(309, 200, 6, 8, 26, 'PETIT_DEJEUNER', 1, 1, 1, 0, 0, 1, 0),
(310, 580, 44, 28, 22, 'DINER', 0, 0, 1, 0, 0, 1, 0),
(311, 460, 18, 16, 56, 'DINER', 1, 0, 1, 1, 1, 0, 1),
(312, 180, 6, 6, 24, 'DEJEUNER', 1, 0, 1, 1, 0, 0, 0),
(313, 320, 4, 22, 26, 'PETIT_DEJEUNER', 1, 0, 1, 0, 1, 0, 0),
(314, 140, 18, 4, 2, 'PETIT_DEJEUNER', 1, 0, 1, 0, 0, 0, 1),
(315, 420, 14, 10, 68, 'DINER', 1, 1, 1, 1, 0, 0, 0),
(316, 10, 0, 0, 2, 'PETIT_DEJEUNER', 1, 1, 1, 0, 0, 0, 0),
(317, 140, 4, 12, 6, 'DEJEUNER', 1, 0, 1, 0, 1, 0, 1),
(318, 380, 6, 18, 48, 'PETIT_DEJEUNER', 1, 0, 1, 1, 1, 0, 1),
(319, 180, 10, 12, 8, 'DINER', 1, 0, 1, 0, 1, 0, 0),
(320, 420, 32, 20, 24, 'DEJEUNER', 0, 0, 1, 1, 0, 0, 0),
(321, 80, 1, 0, 18, 'PETIT_DEJEUNER', 1, 1, 1, 0, 0, 0, 0),
(322, 190, 6, 6, 26, 'DINER', 1, 1, 1, 0, 0, 0, 0),
(323, 360, 8, 18, 42, 'PETIT_DEJEUNER', 1, 0, 1, 1, 1, 0, 1),
(324, 300, 16, 14, 24, 'DEJEUNER', 1, 1, 1, 0, 0, 0, 1),
(325, 340, 6, 10, 56, 'DINER', 1, 1, 1, 0, 0, 0, 0),
(326, 80, 6, 2, 10, 'DINER', 1, 1, 1, 0, 0, 0, 0),
(327, 280, 6, 14, 32, 'PETIT_DEJEUNER', 1, 0, 1, 1, 1, 0, 0),
(328, 10, 0, 0, 2, 'PETIT_DEJEUNER', 1, 1, 1, 0, 0, 0, 0),
(329, 320, 10, 8, 52, 'PETIT_DEJEUNER', 1, 1, 1, 1, 0, 1, 0);

-- --------------------------------------------------------

--
-- Structure de la table `recette_ingredient`
--

CREATE TABLE `recette_ingredient` (
  `id` int(11) NOT NULL,
  `quantite` varchar(50) NOT NULL,
  `recette_id` int(11) NOT NULL,
  `ingredient_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `recette_ingredient`
--

INSERT INTO `recette_ingredient` (`id`, `quantite`, `recette_id`, `ingredient_id`) VALUES
(130, '2', 24, 63),
(132, '2 pcs', 1, 44),
(133, '100g', 1, 45),
(134, '4 pcs', 12, 42),
(138, '3 pcs', 15, 51),
(139, '50g', 15, 45),
(140, '4 pcs', 16, 44),
(141, '1 pcs', 16, 48),
(142, '400g', 19, 52),
(143, '200g', 19, 45),
(144, '3 pcs', 19, 44),
(145, '200g', 20, 52),
(146, '2 pcs', 20, 51),
(147, '300ml', 20, 50),
(148, '100g', 20, 54),
(149, '250g', 21, 52),
(150, '3 pcs', 21, 51),
(151, '250ml', 21, 50),
(152, '150g', 21, 54),
(153, '200g', 22, 54),
(154, '150g', 22, 53),
(155, '100g', 22, 52),
(156, '3 pcs', 22, 51),
(157, '300g', 13, 52),
(158, '200g', 13, 45),
(159, '4', 13, 15);

-- --------------------------------------------------------

--
-- Structure de la table `recette_sauvegardee`
--

CREATE TABLE `recette_sauvegardee` (
  `id` int(11) NOT NULL,
  `external_id` varchar(50) NOT NULL,
  `title` varchar(255) NOT NULL,
  `image` varchar(500) DEFAULT NULL,
  `ready_in_minutes` int(11) DEFAULT NULL,
  `used_ingredients` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`used_ingredients`)),
  `missed_ingredients` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`missed_ingredients`)),
  `saved_at` datetime NOT NULL,
  `etapes` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`etapes`)),
  `recettes_similaires` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`recettes_similaires`)),
  `is_favorite` tinyint(4) NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `reminder_schedule`
--

CREATE TABLE `reminder_schedule` (
  `id` int(11) NOT NULL,
  `reminder_type` varchar(50) NOT NULL,
  `message` longtext NOT NULL,
  `scheduled_time` time NOT NULL,
  `days_of_week` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`days_of_week`)),
  `is_active` tinyint(4) NOT NULL,
  `next_send_at` datetime DEFAULT NULL,
  `last_sent_at` datetime DEFAULT NULL,
  `sent_count` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `session_evenement`
--

CREATE TABLE `session_evenement` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `start_at` datetime DEFAULT NULL,
  `end_at` datetime DEFAULT NULL,
  `speaker` varchar(255) DEFAULT NULL,
  `evenement_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `sms_log`
--

CREATE TABLE `sms_log` (
  `id` int(11) NOT NULL,
  `phone_number` varchar(20) NOT NULL,
  `message` longtext NOT NULL,
  `direction` varchar(50) NOT NULL,
  `status` varchar(50) NOT NULL,
  `twilio_sid` varchar(100) DEFAULT NULL,
  `message_type` varchar(50) DEFAULT NULL,
  `error_message` longtext DEFAULT NULL,
  `error_code` int(11) DEFAULT NULL,
  `sent_at` datetime NOT NULL,
  `delivered_at` datetime DEFAULT NULL,
  `metadata` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`metadata`)),
  `cost` decimal(10,4) DEFAULT NULL,
  `currency` varchar(10) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `sms_log`
--

INSERT INTO `sms_log` (`id`, `phone_number`, `message`, `direction`, `status`, `twilio_sid`, `message_type`, `error_message`, `error_code`, `sent_at`, `delivered_at`, `metadata`, `cost`, `currency`, `user_id`) VALUES
(1, '+21697016138', 'Votre code de vérification est: 297314\n\nCe code expire dans 10 minutes.', 'outbound', 'failed', NULL, 'otp', '[HTTP 400] Unable to create record: The number +2169701XXXX is unverified. Trial accounts cannot send messages to unverified numbers; verify +2169701XXXX at twilio.com/user/account/phone-numbers/verified, or purchase a Twilio number to send messages to unverified numbers', 21608, '2026-03-04 01:08:32', NULL, '[]', NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `sponsor`
--

CREATE TABLE `sponsor` (
  `id` int(11) NOT NULL,
  `nom_partenaire` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `description` varchar(255) NOT NULL,
  `statu` varchar(255) NOT NULL,
  `logo` varchar(255) DEFAULT NULL,
  `evenement_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

CREATE TABLE `user` (
  `id` int(11) NOT NULL,
  `email` varchar(180) NOT NULL,
  `roles` varchar(50) NOT NULL DEFAULT 'ROLE_USER',
  `password` varchar(255) NOT NULL,
  `is_active` tinyint(4) NOT NULL,
  `created_at` datetime NOT NULL,
  `reset_token` varchar(255) DEFAULT NULL,
  `reset_token_expires_at` datetime DEFAULT NULL,
  `photo_filename` varchar(255) DEFAULT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `verification_code` varchar(6) DEFAULT NULL,
  `verification_code_expires_at` datetime DEFAULT NULL,
  `face_descriptor` longtext DEFAULT NULL,
  `face_id_enrolled_at` datetime DEFAULT NULL,
  `welcome_message` longtext DEFAULT NULL,
  `google_id` varchar(255) DEFAULT NULL,
  `facebook_id` varchar(255) DEFAULT NULL,
  `first_name` varchar(100) NOT NULL DEFAULT '',
  `last_name` varchar(100) NOT NULL DEFAULT '',
  `birthday` date NOT NULL DEFAULT '2000-01-01',
  `weight` float DEFAULT NULL,
  `height` float DEFAULT NULL,
  `phone_number` varchar(20) DEFAULT NULL,
  `phone_verified` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `user`
--

INSERT INTO `user` (`id`, `email`, `roles`, `password`, `is_active`, `created_at`, `reset_token`, `reset_token_expires_at`, `photo_filename`, `avatar_url`, `verification_code`, `verification_code_expires_at`, `face_descriptor`, `face_id_enrolled_at`, `welcome_message`, `google_id`, `facebook_id`, `first_name`, `last_name`, `birthday`, `weight`, `height`, `phone_number`, `phone_verified`) VALUES
(1, 'kodra.fakraoui@esprit.tn', 'ROLE_USER', '$2y$13$1eyiKRbamlfTTCI3qMKxteuKJ6WJhYzV/M9ujHQSaBBgUB3MrRM7m', 1, '2026-02-25 23:53:49', NULL, NULL, NULL, NULL, '323016', '2026-02-26 00:11:18', NULL, NULL, NULL, '114312769689263115549', NULL, '', '', '2000-01-01', NULL, NULL, NULL, 0),
(2, 'tttyttti261@gmail.com', 'ROLE_ADMIN', '$2y$13$vJt07L/aCBNjP4Y2/JdvceA9twY0m2hmit6D5Ndm8zNPulU7Xp.I2', 1, '2026-02-24 21:31:34', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '', '2000-01-01', NULL, NULL, NULL, 0),
(4, 'kodrafakraoui@gmail.com', 'ROLE_USER', '$2y$13$IVk7Quh2f77VZtnyrhpjDO45Oa9lo/wt9PJ5hHeyCvF2aBSk4sPPe', 1, '2026-02-25 21:42:57', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '', '2000-01-01', NULL, NULL, NULL, 0),
(5, 'hamzabezzin@gmail.com', 'ROLE_USER', '$2y$13$Um2XoxYR4YJqLAHRSTHKIOR1kO8p6YtV9rffJDa0kCxJhmAaqJsKK', 1, '2026-02-25 21:43:47', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '', '2000-01-01', NULL, NULL, NULL, 0),
(6, 'emnabelhsan@gmail.com', 'ROLE_USER', '$2y$13$W5SjiOE53z0jpoKUBIcNIebC.gCQXNEeOfjt3j0XuFUn5tFqjfwlm', 1, '2026-02-25 22:53:10', NULL, NULL, NULL, NULL, '529343', '2026-02-25 23:14:23', NULL, '2026-02-25 22:53:18', NULL, NULL, NULL, '', '', '2000-01-01', NULL, NULL, NULL, 0),
(7, 'emna.belhassen@esprit.tn', 'ROLE_ADMIN', '$2y$13$RHYY9kK718pGHzUqY9q0Devz90S1DqNCV/QNn7aDGdLypkYmwqusO', 1, '2026-02-25 23:03:04', NULL, NULL, NULL, NULL, '492403', '2026-02-25 23:54:05', NULL, NULL, NULL, NULL, NULL, '', '', '2000-01-01', NULL, NULL, NULL, 0),
(8, 'salimsghaier159@gmail.com', 'ROLE_USER', '$2y$13$ecNSuXhL8fauef5i2uUeQO3dEN8Lx03JFXx0SGbblwffR2F1PO1Zu', 1, '2026-02-25 23:38:26', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '114770511158961717368', NULL, '', '', '2000-01-01', NULL, NULL, NULL, 0),
(9, 'maram.mr@gmail.com', 'ROLE_ADMIN', '$2y$13$s.UUX.EMbs4W6ZLyaT6zt.v7o8MUmaXaxIB.sovuMvP9x/TEtjHay', 1, '2026-03-04 01:08:25', NULL, NULL, NULL, 'https://api.dicebear.com/9.x/avataaars/svg?seed=maram.mr%40gmail.com&backgroundColor=transparent', NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', '', '2000-01-01', NULL, NULL, NULL, 0),
(11, 'maramkod@gmail.com', 'ROLE_USER', '$2a$12$drEZME2aK5sNOmOxZP1/e.3MZ3/ajNdPSiaAfa9bN/aZ5JipzyznC', 1, '2026-04-14 00:31:02', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '', NULL, NULL, 'kodra', 'fakraoui', '2003-06-08', 80, 170, NULL, 0),
(12, 'inchirah@gmail.com', 'ROLE_ADMIN', '$2a$12$swb/1e8wgBo9vPaeRk5Q5ObePS1ZYCAJhv91.A3THtpaeUkAE.RuG', 1, '2026-04-14 00:36:12', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'hi', NULL, NULL, 'inchirah', 'fakraoui', '2005-05-08', 80, 180, NULL, 0),
(15, 'test@gmail.com', 'ROLE_USER', '$2a$12$M26B5kYQ/ccSDFdOf9uq8ODSZVasLFQPwaiyd97Cs9lfZ0NTDpQb6', 1, '2026-04-14 01:56:45', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'test', 'tiw', '2002-03-02', 78, 150, NULL, 0),
(16, 'user@nutrilife.com', 'ROLE_USER', '$2a$12$paP0IGvC7NZg7WXYcihf7OQuLWVXMm99fk0UYP/M4we4JBWFefyBC', 1, '2026-04-22 18:19:28', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Demo', 'User', '1995-06-15', 70, 175, NULL, 0),
(17, 'salim@gmail.com', 'ROLE_ADMIN', '$2a$12$XQXs0bH6QLE47P5Q37AtMeFpwnOMvpi9mP8mkIB.4MocdvHWX7uj2', 1, '2026-04-24 11:27:14', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Salim', 'Admin', '1995-01-01', 70, 175, NULL, 0),
(19, 'isra@gmail.com', 'ROLE_USER', '$2a$12$QJqPH.6FJr.a0bnhzpfkKOKQYhVX8Vxyv0R0Tn3EgzCm1gSgo.9Vq', 1, '2026-04-24 14:01:06', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'isra', 'isra', '2007-04-04', 80, 170, '+21635456789', 0);

-- --------------------------------------------------------

--
-- Structure de la table `user_badge`
--

CREATE TABLE `user_badge` (
  `id` int(11) NOT NULL,
  `unlocked` tinyint(4) NOT NULL,
  `unlocked_at` datetime DEFAULT NULL,
  `current_value` int(11) NOT NULL,
  `is_vitrine` tinyint(4) NOT NULL DEFAULT 0,
  `user_id` int(11) NOT NULL,
  `badge_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `user_badge`
--

INSERT INTO `user_badge` (`id`, `unlocked`, `unlocked_at`, `current_value`, `is_vitrine`, `user_id`, `badge_id`) VALUES
(1, 0, NULL, 0, 0, 1, 1),
(2, 0, NULL, 0, 0, 1, 2),
(3, 0, NULL, 0, 0, 1, 3),
(4, 0, NULL, 0, 0, 1, 4),
(5, 0, NULL, 0, 0, 1, 5),
(6, 0, NULL, 0, 0, 1, 6),
(7, 0, NULL, 0, 0, 1, 7),
(8, 0, NULL, 0, 0, 1, 8),
(9, 0, NULL, 0, 0, 1, 9),
(10, 0, NULL, 0, 0, 1, 10),
(11, 0, NULL, 0, 0, 1, 11),
(12, 0, NULL, 0, 0, 11, 1),
(13, 0, NULL, 0, 0, 11, 2),
(14, 0, NULL, 0, 0, 11, 3),
(15, 0, NULL, 0, 0, 11, 4),
(16, 0, NULL, 0, 0, 11, 5),
(17, 0, NULL, 0, 0, 11, 6),
(18, 0, NULL, 0, 0, 11, 7),
(19, 0, NULL, 0, 0, 11, 8),
(20, 0, NULL, 0, 0, 11, 9),
(21, 0, NULL, 0, 0, 11, 10),
(22, 0, NULL, 0, 0, 11, 11),
(23, 0, NULL, 0, 0, 19, 101),
(24, 1, '2026-04-24 22:03:21', 1, 0, 19, 102),
(25, 0, NULL, 1, 0, 19, 103),
(26, 0, NULL, 0, 0, 19, 104),
(27, 0, NULL, 0, 0, 19, 105),
(28, 0, NULL, 0, 0, 19, 106),
(29, 0, NULL, 0, 0, 19, 107),
(30, 0, NULL, 1, 0, 19, 108),
(31, 0, NULL, 385, 0, 19, 109),
(32, 0, NULL, 0, 0, 12, 101),
(33, 0, NULL, 0, 0, 12, 102),
(34, 0, NULL, 0, 0, 12, 103),
(35, 0, NULL, 0, 0, 12, 104),
(36, 0, NULL, 0, 0, 12, 105),
(37, 0, NULL, 0, 0, 12, 106),
(38, 0, NULL, 0, 0, 12, 107),
(39, 0, NULL, 0, 0, 12, 108),
(40, 0, NULL, 0, 0, 12, 109),
(41, 0, NULL, 0, 0, 11, 101),
(42, 0, NULL, 0, 0, 11, 102),
(43, 0, NULL, 0, 0, 11, 103),
(44, 0, NULL, 0, 0, 11, 104),
(45, 0, NULL, 0, 0, 11, 105),
(46, 0, NULL, 0, 0, 11, 106),
(47, 0, NULL, 0, 0, 11, 107),
(48, 0, NULL, 0, 0, 11, 108),
(49, 0, NULL, 0, 0, 11, 109);

-- --------------------------------------------------------

--
-- Structure de la table `user_consent`
--

CREATE TABLE `user_consent` (
  `id` int(11) NOT NULL,
  `consent_type` varchar(50) NOT NULL,
  `is_granted` tinyint(4) NOT NULL,
  `granted_at` datetime NOT NULL,
  `revoked_at` datetime DEFAULT NULL,
  `ip_address` varchar(50) DEFAULT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  `notes` longtext DEFAULT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `webauthn_credentials`
--

CREATE TABLE `webauthn_credentials` (
  `id` int(11) NOT NULL,
  `credential_id` varchar(255) NOT NULL,
  `public_key` longtext NOT NULL,
  `counter` int(11) NOT NULL,
  `device_name` varchar(100) NOT NULL,
  `created_at` datetime NOT NULL,
  `last_used_at` datetime DEFAULT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `weight_log`
--

CREATE TABLE `weight_log` (
  `id` int(11) NOT NULL,
  `weight` decimal(5,2) NOT NULL,
  `recorded_at` datetime NOT NULL,
  `notes` longtext DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `user_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `additives_danger`
--
ALTER TABLE `additives_danger`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `code` (`code`),
  ADD KEY `idx_code` (`code`);

--
-- Index pour la table `badge`
--
ALTER TABLE `badge`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `boycott_brands`
--
ALTER TABLE `boycott_brands`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_brand_name` (`brand_name`);

--
-- Index pour la table `daily_log`
--
ALTER TABLE `daily_log`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_8D0D8EA9D6594DD6` (`share_token`),
  ADD KEY `IDX_8D0D8EA992281DB5` (`nutrition_objective_id`);

--
-- Index pour la table `doctrine_migration_versions`
--
ALTER TABLE `doctrine_migration_versions`
  ADD PRIMARY KEY (`version`);

--
-- Index pour la table `evenement`
--
ALTER TABLE `evenement`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `face_embeddings`
--
ALTER TABLE `face_embeddings`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_87E90A04A76ED395` (`user_id`);

--
-- Index pour la table `face_verification_attempts`
--
ALTER TABLE `face_verification_attempts`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_D98ED909A76ED395` (`user_id`),
  ADD KEY `idx_ip_time` (`ip_address`,`attempted_at`),
  ADD KEY `idx_user_time` (`user_id`,`attempted_at`);

--
-- Index pour la table `gaspillage_log`
--
ALTER TABLE `gaspillage_log`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_DC2B9851A76ED395` (`user_id`);

--
-- Index pour la table `ingredient`
--
ALTER TABLE `ingredient`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `liste_courses`
--
ALTER TABLE `liste_courses`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `meal_plan`
--
ALTER TABLE `meal_plan`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_mp_user` (`user_id`);

--
-- Index pour la table `meal_plan_item`
--
ALTER TABLE `meal_plan_item`
  ADD PRIMARY KEY (`id`),
  ADD KEY `fk_mpi_plan` (`meal_plan_id`),
  ADD KEY `fk_mpi_recette` (`recette_id`);

--
-- Index pour la table `nutrition_objective`
--
ALTER TABLE `nutrition_objective`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_461C07E5A76ED395` (`user_id`);

--
-- Index pour la table `participation`
--
ALTER TABLE `participation`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uniq_evenement_email` (`evenement_id`,`email`),
  ADD KEY `IDX_AB55E24FFD02F13` (`evenement_id`);

--
-- Index pour la table `profile`
--
ALTER TABLE `profile`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_8157AA0FA76ED395` (`user_id`);

--
-- Index pour la table `progress_photo`
--
ALTER TABLE `progress_photo`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_F60F7723A76ED395` (`user_id`),
  ADD KEY `idx_photo_user_date` (`user_id`,`taken_at`);

--
-- Index pour la table `reaction`
--
ALTER TABLE `reaction`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_A4D707F7FD02F13` (`evenement_id`);

--
-- Index pour la table `recette`
--
ALTER TABLE `recette`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_49BB6390A76ED395` (`user_id`);

--
-- Index pour la table `recette_favoris`
--
ALTER TABLE `recette_favoris`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_recette_fav` (`user_id`,`recette_id`),
  ADD KEY `IDX_4D4B1E48A76ED395` (`user_id`),
  ADD KEY `IDX_4D4B1E4889312FE9` (`recette_id`);

--
-- Index pour la table `recette_info_plus`
--
ALTER TABLE `recette_info_plus`
  ADD PRIMARY KEY (`recette_id`);

--
-- Index pour la table `recette_ingredient`
--
ALTER TABLE `recette_ingredient`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_17C041A989312FE9` (`recette_id`),
  ADD KEY `IDX_17C041A9933FE08C` (`ingredient_id`);

--
-- Index pour la table `recette_sauvegardee`
--
ALTER TABLE `recette_sauvegardee`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_recipe` (`user_id`,`external_id`),
  ADD KEY `IDX_DE8DF217A76ED395` (`user_id`);

--
-- Index pour la table `reminder_schedule`
--
ALTER TABLE `reminder_schedule`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_384B166DA76ED395` (`user_id`),
  ADD KEY `idx_reminder_active` (`is_active`),
  ADD KEY `idx_reminder_next_send` (`next_send_at`);

--
-- Index pour la table `session_evenement`
--
ALTER TABLE `session_evenement`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_48C42D14FD02F13` (`evenement_id`);

--
-- Index pour la table `sms_log`
--
ALTER TABLE `sms_log`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_A9E43D70A76ED395` (`user_id`),
  ADD KEY `idx_sms_status` (`status`),
  ADD KEY `idx_sms_sent_at` (`sent_at`);

--
-- Index pour la table `sponsor`
--
ALTER TABLE `sponsor`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_818CC9D4FD02F13` (`evenement_id`);

--
-- Index pour la table `user`
--
ALTER TABLE `user`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_8D93D649E7927C74` (`email`);

--
-- Index pour la table `user_badge`
--
ALTER TABLE `user_badge`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_1C32B345A76ED395F7A2C2FC` (`user_id`,`badge_id`),
  ADD KEY `IDX_1C32B345A76ED395` (`user_id`),
  ADD KEY `IDX_1C32B345F7A2C2FC` (`badge_id`);

--
-- Index pour la table `user_consent`
--
ALTER TABLE `user_consent`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `unique_user_consent_type` (`user_id`,`consent_type`),
  ADD KEY `IDX_3B1F161AA76ED395` (`user_id`);

--
-- Index pour la table `webauthn_credentials`
--
ALTER TABLE `webauthn_credentials`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_DFEA84902558A7A5` (`credential_id`),
  ADD KEY `IDX_DFEA8490A76ED395` (`user_id`);

--
-- Index pour la table `weight_log`
--
ALTER TABLE `weight_log`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_6BBB9E9CA76ED395` (`user_id`),
  ADD KEY `idx_weight_user_date` (`user_id`,`recorded_at`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `additives_danger`
--
ALTER TABLE `additives_danger`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=65;

--
-- AUTO_INCREMENT pour la table `badge`
--
ALTER TABLE `badge`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=110;

--
-- AUTO_INCREMENT pour la table `boycott_brands`
--
ALTER TABLE `boycott_brands`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=118;

--
-- AUTO_INCREMENT pour la table `daily_log`
--
ALTER TABLE `daily_log`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT pour la table `evenement`
--
ALTER TABLE `evenement`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `face_embeddings`
--
ALTER TABLE `face_embeddings`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `face_verification_attempts`
--
ALTER TABLE `face_verification_attempts`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `gaspillage_log`
--
ALTER TABLE `gaspillage_log`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `ingredient`
--
ALTER TABLE `ingredient`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=101;

--
-- AUTO_INCREMENT pour la table `liste_courses`
--
ALTER TABLE `liste_courses`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT pour la table `meal_plan`
--
ALTER TABLE `meal_plan`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `meal_plan_item`
--
ALTER TABLE `meal_plan_item`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=43;

--
-- AUTO_INCREMENT pour la table `nutrition_objective`
--
ALTER TABLE `nutrition_objective`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `participation`
--
ALTER TABLE `participation`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `profile`
--
ALTER TABLE `profile`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT pour la table `progress_photo`
--
ALTER TABLE `progress_photo`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `reaction`
--
ALTER TABLE `reaction`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `recette`
--
ALTER TABLE `recette`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=330;

--
-- AUTO_INCREMENT pour la table `recette_favoris`
--
ALTER TABLE `recette_favoris`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT pour la table `recette_ingredient`
--
ALTER TABLE `recette_ingredient`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=160;

--
-- AUTO_INCREMENT pour la table `recette_sauvegardee`
--
ALTER TABLE `recette_sauvegardee`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `reminder_schedule`
--
ALTER TABLE `reminder_schedule`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `session_evenement`
--
ALTER TABLE `session_evenement`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `sms_log`
--
ALTER TABLE `sms_log`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT pour la table `sponsor`
--
ALTER TABLE `sponsor`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `user`
--
ALTER TABLE `user`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT pour la table `user_badge`
--
ALTER TABLE `user_badge`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=50;

--
-- AUTO_INCREMENT pour la table `user_consent`
--
ALTER TABLE `user_consent`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `webauthn_credentials`
--
ALTER TABLE `webauthn_credentials`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `weight_log`
--
ALTER TABLE `weight_log`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `daily_log`
--
ALTER TABLE `daily_log`
  ADD CONSTRAINT `FK_8D0D8EA992281DB5` FOREIGN KEY (`nutrition_objective_id`) REFERENCES `nutrition_objective` (`id`);

--
-- Contraintes pour la table `face_embeddings`
--
ALTER TABLE `face_embeddings`
  ADD CONSTRAINT `FK_87E90A04A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `face_verification_attempts`
--
ALTER TABLE `face_verification_attempts`
  ADD CONSTRAINT `FK_D98ED909A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE SET NULL;

--
-- Contraintes pour la table `gaspillage_log`
--
ALTER TABLE `gaspillage_log`
  ADD CONSTRAINT `FK_DC2B9851A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `meal_plan`
--
ALTER TABLE `meal_plan`
  ADD CONSTRAINT `fk_mp_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `meal_plan_item`
--
ALTER TABLE `meal_plan_item`
  ADD CONSTRAINT `fk_mpi_plan` FOREIGN KEY (`meal_plan_id`) REFERENCES `meal_plan` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_mpi_recette` FOREIGN KEY (`recette_id`) REFERENCES `recette` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `nutrition_objective`
--
ALTER TABLE `nutrition_objective`
  ADD CONSTRAINT `FK_461C07E5A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `participation`
--
ALTER TABLE `participation`
  ADD CONSTRAINT `FK_AB55E24FFD02F13` FOREIGN KEY (`evenement_id`) REFERENCES `evenement` (`id`);

--
-- Contraintes pour la table `profile`
--
ALTER TABLE `profile`
  ADD CONSTRAINT `FK_8157AA0FA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `progress_photo`
--
ALTER TABLE `progress_photo`
  ADD CONSTRAINT `FK_F60F7723A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `reaction`
--
ALTER TABLE `reaction`
  ADD CONSTRAINT `FK_A4D707F7FD02F13` FOREIGN KEY (`evenement_id`) REFERENCES `evenement` (`id`);

--
-- Contraintes pour la table `recette`
--
ALTER TABLE `recette`
  ADD CONSTRAINT `FK_49BB6390A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `recette_favoris`
--
ALTER TABLE `recette_favoris`
  ADD CONSTRAINT `FK_4D4B1E4889312FE9` FOREIGN KEY (`recette_id`) REFERENCES `recette` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_4D4B1E48A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `recette_info_plus`
--
ALTER TABLE `recette_info_plus`
  ADD CONSTRAINT `fk_rip_recette` FOREIGN KEY (`recette_id`) REFERENCES `recette` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Contraintes pour la table `recette_ingredient`
--
ALTER TABLE `recette_ingredient`
  ADD CONSTRAINT `FK_17C041A989312FE9` FOREIGN KEY (`recette_id`) REFERENCES `recette` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_17C041A9933FE08C` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredient` (`id`);

--
-- Contraintes pour la table `recette_sauvegardee`
--
ALTER TABLE `recette_sauvegardee`
  ADD CONSTRAINT `FK_DE8DF217A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `reminder_schedule`
--
ALTER TABLE `reminder_schedule`
  ADD CONSTRAINT `FK_384B166DA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `session_evenement`
--
ALTER TABLE `session_evenement`
  ADD CONSTRAINT `FK_48C42D14FD02F13` FOREIGN KEY (`evenement_id`) REFERENCES `evenement` (`id`);

--
-- Contraintes pour la table `sms_log`
--
ALTER TABLE `sms_log`
  ADD CONSTRAINT `FK_A9E43D70A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `sponsor`
--
ALTER TABLE `sponsor`
  ADD CONSTRAINT `FK_818CC9D4FD02F13` FOREIGN KEY (`evenement_id`) REFERENCES `evenement` (`id`);

--
-- Contraintes pour la table `user_badge`
--
ALTER TABLE `user_badge`
  ADD CONSTRAINT `FK_1C32B345A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  ADD CONSTRAINT `FK_1C32B345F7A2C2FC` FOREIGN KEY (`badge_id`) REFERENCES `badge` (`id`);

--
-- Contraintes pour la table `user_consent`
--
ALTER TABLE `user_consent`
  ADD CONSTRAINT `FK_3B1F161AA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `webauthn_credentials`
--
ALTER TABLE `webauthn_credentials`
  ADD CONSTRAINT `FK_DFEA8490A76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);

--
-- Contraintes pour la table `weight_log`
--
ALTER TABLE `weight_log`
  ADD CONSTRAINT `FK_6BBB9E9CA76ED395` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`);
COMMIT;

--
-- COMPLAINT MODULE TABLES (Integrated)
--
CREATE TABLE IF NOT EXISTS `complaint` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `user_id` INT NOT NULL,
  `title` VARCHAR(150) NOT NULL,
  `description` TEXT NOT NULL,
  `phone_number` VARCHAR(20),
  
ate INT DEFAULT 0,
  `date_of_complaint` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  `image_path` TEXT,
  `incident_date` DATE,
  `detected_emotion` VARCHAR(50) DEFAULT 'NEUTRAL',
  `emotion_score` DOUBLE DEFAULT 0,
  `urgency_level` INT DEFAULT 1,
  `emotion_recommendation` TEXT,
  CONSTRAINT `FK_complaint_user_final` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `complaint_response` (
  `id` INT AUTO_INCREMENT PRIMARY KEY,
  `complaint_id` INT NOT NULL,
  `response_content` TEXT NOT NULL,
  `response_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT `FK_resp_complaint_final` FOREIGN KEY (`complaint_id`) REFERENCES `complaint` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- MISSING COLUMNS PATCH
--
ALTER TABLE `user_badge` ADD COLUMN IF NOT EXISTS `unlocked` TINYINT(1) DEFAULT 1;
ALTER TABLE `user_badge` ADD COLUMN IF NOT EXISTS `unlocked_at` DATETIME DEFAULT CURRENT_TIMESTAMP;

COMMIT;
