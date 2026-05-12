-- StudyRoom MySQL 8 initialization script
-- This script is destructive: it recreates the study_room database from scratch.

SET NAMES utf8mb4;
SET time_zone = '+00:00';

DROP DATABASE IF EXISTS study_room;
CREATE DATABASE study_room
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;

USE study_room;

-- =========================================================
-- 1. Organization and identity domain
-- =========================================================

CREATE TABLE campuses (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  campus_code VARCHAR(32) NOT NULL,
  campus_name VARCHAR(128) NOT NULL,
  campus_status ENUM('active', 'inactive') NOT NULL DEFAULT 'active',
  address VARCHAR(255) NULL,
  timezone_name VARCHAR(64) NOT NULL DEFAULT 'Asia/Shanghai',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_campuses_code (campus_code)
) ENGINE=InnoDB;

CREATE TABLE organizations (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  campus_id BIGINT UNSIGNED NOT NULL,
  parent_id BIGINT UNSIGNED NULL,
  org_code VARCHAR(32) NOT NULL,
  org_name VARCHAR(128) NOT NULL,
  org_type VARCHAR(32) NOT NULL,
  org_path VARCHAR(512) NOT NULL,
  display_order INT NOT NULL DEFAULT 0,
  org_status ENUM('active', 'inactive') NOT NULL DEFAULT 'active',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_organizations_code (org_code),
  KEY idx_organizations_campus_parent (campus_id, parent_id),
  KEY idx_organizations_path (org_path),
  CONSTRAINT fk_organizations_campus FOREIGN KEY (campus_id) REFERENCES campuses (id),
  CONSTRAINT fk_organizations_parent FOREIGN KEY (parent_id) REFERENCES organizations (id)
) ENGINE=InnoDB;

CREATE TABLE users (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_type ENUM('student', 'teacher', 'staff', 'mixed') NOT NULL,
  account_status ENUM('active', 'inactive', 'suspended', 'locked') NOT NULL DEFAULT 'active',
  student_no VARCHAR(32) NULL,
  employee_no VARCHAR(32) NULL,
  full_name VARCHAR(64) NOT NULL,
  nickname VARCHAR(64) NULL,
  email VARCHAR(128) NULL,
  phone VARCHAR(32) NULL,
  avatar_url VARCHAR(255) NULL,
  primary_organization_id BIGINT UNSIGNED NULL,
  must_change_password TINYINT(1) NOT NULL DEFAULT 0,
  violation_count INT UNSIGNED NOT NULL DEFAULT 0,
  last_login_at DATETIME NULL,
  last_active_at DATETIME NULL,
  remark VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_student_no (student_no),
  UNIQUE KEY uk_users_employee_no (employee_no),
  UNIQUE KEY uk_users_email (email),
  UNIQUE KEY uk_users_phone (phone),
  KEY idx_users_org_status (primary_organization_id, account_status),
  CONSTRAINT fk_users_primary_org FOREIGN KEY (primary_organization_id) REFERENCES organizations (id)
) ENGINE=InnoDB;

CREATE TABLE auth_identities (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  identity_type ENUM('student_no_password', 'email_password', 'employee_no_password', 'wechat_openid', 'oauth') NOT NULL,
  identity_key VARCHAR(128) NOT NULL,
  auth_source ENUM('local', 'wechat', 'oauth') NOT NULL DEFAULT 'local',
  credential_hash VARCHAR(255) NULL,
  credential_salt VARCHAR(128) NULL,
  credential_version SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  is_verified TINYINT(1) NOT NULL DEFAULT 0,
  verified_at DATETIME NULL,
  last_used_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_auth_identity (identity_type, identity_key),
  KEY idx_auth_identities_user (user_id),
  CONSTRAINT fk_auth_identities_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE user_contact_methods (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  contact_type ENUM('email', 'phone', 'wechat_service', 'in_app') NOT NULL,
  contact_value VARCHAR(128) NOT NULL,
  is_primary TINYINT(1) NOT NULL DEFAULT 0,
  is_verified TINYINT(1) NOT NULL DEFAULT 0,
  verified_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_contact (contact_type, contact_value),
  KEY idx_user_contact_user (user_id, contact_type),
  CONSTRAINT fk_user_contact_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE user_notification_preferences (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  channel ENUM('email', 'mini_program', 'in_app', 'sms') NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  is_enabled TINYINT(1) NOT NULL DEFAULT 1,
  quiet_hours_start TIME NULL,
  quiet_hours_end TIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_notification_pref (user_id, channel, event_type),
  CONSTRAINT fk_user_notification_pref_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

-- =========================================================
-- 2. RBAC domain
-- =========================================================

CREATE TABLE roles (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_code VARCHAR(64) NOT NULL,
  role_name VARCHAR(128) NOT NULL,
  role_description VARCHAR(255) NULL,
  role_status ENUM('active', 'inactive') NOT NULL DEFAULT 'active',
  is_system TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_roles_code (role_code)
) ENGINE=InnoDB;

CREATE TABLE permissions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  permission_code VARCHAR(64) NOT NULL,
  permission_name VARCHAR(128) NOT NULL,
  resource_code VARCHAR(64) NOT NULL,
  action_code VARCHAR(64) NOT NULL,
  permission_description VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_permissions_code (permission_code),
  UNIQUE KEY uk_permissions_resource_action (resource_code, action_code)
) ENGINE=InnoDB;

CREATE TABLE role_permissions (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  role_id BIGINT UNSIGNED NOT NULL,
  permission_id BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_role_permissions (role_id, permission_id),
  CONSTRAINT fk_role_permissions_role FOREIGN KEY (role_id) REFERENCES roles (id),
  CONSTRAINT fk_role_permissions_permission FOREIGN KEY (permission_id) REFERENCES permissions (id)
) ENGINE=InnoDB;

CREATE TABLE user_roles (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  role_id BIGINT UNSIGNED NOT NULL,
  scope_type ENUM('global', 'campus', 'organization') NOT NULL DEFAULT 'global',
  campus_id BIGINT UNSIGNED NULL,
  organization_id BIGINT UNSIGNED NULL,
  is_primary TINYINT(1) NOT NULL DEFAULT 0,
  effective_from DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  effective_to DATETIME NULL,
  assigned_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_user_roles_user (user_id),
  KEY idx_user_roles_scope (scope_type, campus_id, organization_id),
  CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id),
  CONSTRAINT fk_user_roles_campus FOREIGN KEY (campus_id) REFERENCES campuses (id),
  CONSTRAINT fk_user_roles_organization FOREIGN KEY (organization_id) REFERENCES organizations (id),
  CONSTRAINT fk_user_roles_assigned_by FOREIGN KEY (assigned_by) REFERENCES users (id)
) ENGINE=InnoDB;

-- =========================================================
-- 3. Space resource domain
-- =========================================================

CREATE TABLE buildings (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  campus_id BIGINT UNSIGNED NOT NULL,
  organization_id BIGINT UNSIGNED NULL,
  building_code VARCHAR(32) NOT NULL,
  building_name VARCHAR(128) NOT NULL,
  address VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_buildings_code (building_code),
  KEY idx_buildings_campus_org (campus_id, organization_id),
  CONSTRAINT fk_buildings_campus FOREIGN KEY (campus_id) REFERENCES campuses (id),
  CONSTRAINT fk_buildings_org FOREIGN KEY (organization_id) REFERENCES organizations (id)
) ENGINE=InnoDB;

CREATE TABLE floors (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  building_id BIGINT UNSIGNED NOT NULL,
  floor_code VARCHAR(32) NOT NULL,
  floor_name VARCHAR(64) NOT NULL,
  floor_number INT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_floors_building_code (building_id, floor_code),
  KEY idx_floors_building_number (building_id, floor_number),
  CONSTRAINT fk_floors_building FOREIGN KEY (building_id) REFERENCES buildings (id)
) ENGINE=InnoDB;

CREATE TABLE room_types (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  room_type_code VARCHAR(32) NOT NULL,
  room_type_name VARCHAR(64) NOT NULL,
  booking_mode ENUM('seat', 'room', 'hybrid') NOT NULL DEFAULT 'seat',
  default_policy_json JSON NULL,
  description_text VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_room_types_code (room_type_code)
) ENGINE=InnoDB;

CREATE TABLE study_rooms (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  campus_id BIGINT UNSIGNED NOT NULL,
  owner_organization_id BIGINT UNSIGNED NULL,
  building_id BIGINT UNSIGNED NOT NULL,
  floor_id BIGINT UNSIGNED NULL,
  room_type_id BIGINT UNSIGNED NOT NULL,
  room_code VARCHAR(32) NOT NULL,
  room_name VARCHAR(128) NOT NULL,
  display_name VARCHAR(128) NULL,
  visibility_scope ENUM('public', 'organization', 'custom') NOT NULL DEFAULT 'public',
  room_status ENUM('draft', 'active', 'inactive', 'closed') NOT NULL DEFAULT 'active',
  location_detail VARCHAR(255) NULL,
  description_text TEXT NULL,
  total_capacity INT UNSIGNED NOT NULL DEFAULT 0,
  open_rule_type ENUM('fixed', 'weekly_schedule', 'custom') NOT NULL DEFAULT 'weekly_schedule',
  map_width INT UNSIGNED NULL,
  map_height INT UNSIGNED NULL,
  policy_json JSON NULL,
  created_by BIGINT UNSIGNED NULL,
  updated_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_study_rooms_code (room_code),
  KEY idx_study_rooms_visibility (campus_id, visibility_scope, room_status),
  KEY idx_study_rooms_org_type (owner_organization_id, room_type_id),
  CONSTRAINT fk_study_rooms_campus FOREIGN KEY (campus_id) REFERENCES campuses (id),
  CONSTRAINT fk_study_rooms_owner_org FOREIGN KEY (owner_organization_id) REFERENCES organizations (id),
  CONSTRAINT fk_study_rooms_building FOREIGN KEY (building_id) REFERENCES buildings (id),
  CONSTRAINT fk_study_rooms_floor FOREIGN KEY (floor_id) REFERENCES floors (id),
  CONSTRAINT fk_study_rooms_room_type FOREIGN KEY (room_type_id) REFERENCES room_types (id),
  CONSTRAINT fk_study_rooms_created_by FOREIGN KEY (created_by) REFERENCES users (id),
  CONSTRAINT fk_study_rooms_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE room_visibility_rules (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  study_room_id BIGINT UNSIGNED NOT NULL,
  allow_type ENUM('campus', 'organization', 'role', 'user') NOT NULL,
  campus_id BIGINT UNSIGNED NULL,
  organization_id BIGINT UNSIGNED NULL,
  role_id BIGINT UNSIGNED NULL,
  user_id BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_room_visibility_room_type (study_room_id, allow_type),
  CONSTRAINT fk_room_visibility_room FOREIGN KEY (study_room_id) REFERENCES study_rooms (id),
  CONSTRAINT fk_room_visibility_campus FOREIGN KEY (campus_id) REFERENCES campuses (id),
  CONSTRAINT fk_room_visibility_org FOREIGN KEY (organization_id) REFERENCES organizations (id),
  CONSTRAINT fk_room_visibility_role FOREIGN KEY (role_id) REFERENCES roles (id),
  CONSTRAINT fk_room_visibility_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE room_open_rules (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  study_room_id BIGINT UNSIGNED NOT NULL,
  rule_kind ENUM('weekly', 'holiday_override', 'date_override') NOT NULL DEFAULT 'weekly',
  weekday_no TINYINT UNSIGNED NULL,
  rule_date DATE NULL,
  opens_at TIME NULL,
  closes_at TIME NULL,
  is_open TINYINT(1) NOT NULL DEFAULT 1,
  effective_from DATE NULL,
  effective_to DATE NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_room_open_rules_room_weekday (study_room_id, weekday_no),
  KEY idx_room_open_rules_room_date (study_room_id, rule_date),
  CONSTRAINT fk_room_open_rules_room FOREIGN KEY (study_room_id) REFERENCES study_rooms (id),
  CONSTRAINT chk_room_open_rules_weekday CHECK (weekday_no IS NULL OR weekday_no BETWEEN 0 AND 6)
) ENGINE=InnoDB;

CREATE TABLE room_assets (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  study_room_id BIGINT UNSIGNED NOT NULL,
  asset_kind VARCHAR(32) NOT NULL,
  asset_name VARCHAR(128) NOT NULL,
  asset_url VARCHAR(255) NOT NULL,
  mime_type VARCHAR(64) NULL,
  sort_order INT NOT NULL DEFAULT 0,
  metadata_json JSON NULL,
  created_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_room_assets_room_kind (study_room_id, asset_kind),
  CONSTRAINT fk_room_assets_room FOREIGN KEY (study_room_id) REFERENCES study_rooms (id),
  CONSTRAINT fk_room_assets_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE seat_maps (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  study_room_id BIGINT UNSIGNED NOT NULL,
  version_no INT UNSIGNED NOT NULL,
  map_status ENUM('draft', 'published', 'archived') NOT NULL DEFAULT 'draft',
  background_url VARCHAR(255) NULL,
  map_width INT UNSIGNED NULL,
  map_height INT UNSIGNED NULL,
  published_at DATETIME NULL,
  created_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_seat_maps_room_version (study_room_id, version_no),
  KEY idx_seat_maps_room_status (study_room_id, map_status),
  CONSTRAINT fk_seat_maps_room FOREIGN KEY (study_room_id) REFERENCES study_rooms (id),
  CONSTRAINT fk_seat_maps_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE seats (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  study_room_id BIGINT UNSIGNED NOT NULL,
  seat_code VARCHAR(32) NOT NULL,
  display_label VARCHAR(64) NOT NULL,
  seat_type VARCHAR(32) NOT NULL DEFAULT 'standard',
  row_no INT NULL,
  col_no INT NULL,
  has_power TINYINT(1) NOT NULL DEFAULT 0,
  is_window_side TINYINT(1) NOT NULL DEFAULT 0,
  is_accessible TINYINT(1) NOT NULL DEFAULT 0,
  seat_status ENUM('active', 'inactive', 'maintenance') NOT NULL DEFAULT 'active',
  is_bookable TINYINT(1) NOT NULL DEFAULT 1,
  map_x DECIMAL(10,2) NULL,
  map_y DECIMAL(10,2) NULL,
  map_width DECIMAL(10,2) NULL,
  map_height DECIMAL(10,2) NULL,
  map_rotation DECIMAL(8,2) NULL,
  metadata_json JSON NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_seats_room_code (study_room_id, seat_code),
  KEY idx_seats_room_status (study_room_id, seat_status, is_bookable),
  KEY idx_seats_room_features (study_room_id, has_power, is_window_side),
  CONSTRAINT fk_seats_room FOREIGN KEY (study_room_id) REFERENCES study_rooms (id)
) ENGINE=InnoDB;

CREATE TABLE seat_features (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  feature_code VARCHAR(32) NOT NULL,
  feature_name VARCHAR(64) NOT NULL,
  value_type ENUM('bool', 'number', 'text', 'json') NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_seat_features_code (feature_code)
) ENGINE=InnoDB;

CREATE TABLE seat_feature_values (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  seat_id BIGINT UNSIGNED NOT NULL,
  feature_id BIGINT UNSIGNED NOT NULL,
  bool_value TINYINT(1) NULL,
  number_value DECIMAL(10,2) NULL,
  text_value VARCHAR(255) NULL,
  json_value JSON NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_seat_feature_values (seat_id, feature_id),
  CONSTRAINT fk_seat_feature_values_seat FOREIGN KEY (seat_id) REFERENCES seats (id),
  CONSTRAINT fk_seat_feature_values_feature FOREIGN KEY (feature_id) REFERENCES seat_features (id)
) ENGINE=InnoDB;

CREATE TABLE seat_map_items (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  seat_map_id BIGINT UNSIGNED NOT NULL,
  item_type ENUM('label', 'area', 'facility', 'path') NOT NULL,
  item_key VARCHAR(64) NOT NULL,
  item_label VARCHAR(128) NULL,
  x DECIMAL(10,2) NOT NULL,
  y DECIMAL(10,2) NOT NULL,
  width DECIMAL(10,2) NULL,
  height DECIMAL(10,2) NULL,
  rotation_angle DECIMAL(8,2) NULL,
  style_json JSON NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_seat_map_items (seat_map_id, item_key),
  CONSTRAINT fk_seat_map_items_map FOREIGN KEY (seat_map_id) REFERENCES seat_maps (id)
) ENGINE=InnoDB;

CREATE TABLE seat_status_windows (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  seat_id BIGINT UNSIGNED NOT NULL,
  window_status ENUM('maintenance', 'disabled', 'reserved_only') NOT NULL,
  starts_at DATETIME NOT NULL,
  ends_at DATETIME NOT NULL,
  reason_text VARCHAR(255) NULL,
  created_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_seat_status_windows_seat_time (seat_id, starts_at, ends_at),
  CONSTRAINT fk_seat_status_windows_seat FOREIGN KEY (seat_id) REFERENCES seats (id),
  CONSTRAINT fk_seat_status_windows_created_by FOREIGN KEY (created_by) REFERENCES users (id),
  CONSTRAINT chk_seat_status_windows_time CHECK (starts_at < ends_at)
) ENGINE=InnoDB;

-- =========================================================
-- 4. Reservation, check-in, violation domain
-- =========================================================

CREATE TABLE reservations (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  reservation_no VARCHAR(32) NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  created_by_user_id BIGINT UNSIGNED NULL,
  study_room_id BIGINT UNSIGNED NOT NULL,
  seat_id BIGINT UNSIGNED NOT NULL,
  reservation_date DATE NOT NULL,
  start_at DATETIME NOT NULL,
  end_at DATETIME NOT NULL,
  start_hour TINYINT UNSIGNED NOT NULL,
  end_hour TINYINT UNSIGNED NOT NULL,
  duration_hours TINYINT UNSIGNED NOT NULL,
  reservation_status ENUM('pending_checkin', 'checked_in', 'cancelled', 'violated', 'completed') NOT NULL DEFAULT 'pending_checkin',
  source_channel ENUM('web', 'mini_program', 'admin', 'assistant', 'batch') NOT NULL DEFAULT 'web',
  cancel_reason_type VARCHAR(32) NULL,
  cancel_reason_note VARCHAR(255) NULL,
  reminder_at DATETIME NULL,
  warning_at DATETIME NULL,
  checkin_deadline_at DATETIME NULL,
  checked_in_at DATETIME NULL,
  completed_at DATETIME NULL,
  cancelled_at DATETIME NULL,
  repeated_from_reservation_id BIGINT UNSIGNED NULL,
  policy_snapshot_json JSON NULL,
  notes_text VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_reservations_no (reservation_no),
  KEY idx_reservations_user_date (user_id, reservation_date, reservation_status),
  KEY idx_reservations_room_date (study_room_id, reservation_date, reservation_status),
  KEY idx_reservations_seat_time (seat_id, start_at, end_at),
  CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_reservations_created_by FOREIGN KEY (created_by_user_id) REFERENCES users (id),
  CONSTRAINT fk_reservations_room FOREIGN KEY (study_room_id) REFERENCES study_rooms (id),
  CONSTRAINT fk_reservations_seat FOREIGN KEY (seat_id) REFERENCES seats (id),
  CONSTRAINT fk_reservations_repeated_from FOREIGN KEY (repeated_from_reservation_id) REFERENCES reservations (id),
  CONSTRAINT chk_reservations_hours CHECK (start_hour < end_hour AND duration_hours = end_hour - start_hour),
  CONSTRAINT chk_reservations_time CHECK (start_at < end_at),
  CONSTRAINT chk_reservations_hour_range CHECK (start_hour BETWEEN 0 AND 23 AND end_hour BETWEEN 1 AND 24)
) ENGINE=InnoDB;

CREATE TABLE reservation_status_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT UNSIGNED NOT NULL,
  from_status VARCHAR(32) NULL,
  to_status VARCHAR(32) NOT NULL,
  change_source ENUM('user', 'admin', 'system', 'assistant') NOT NULL,
  changed_by_user_id BIGINT UNSIGNED NULL,
  change_note VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_reservation_status_logs_reservation (reservation_id, created_at),
  CONSTRAINT fk_reservation_status_logs_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id),
  CONSTRAINT fk_reservation_status_logs_changed_by FOREIGN KEY (changed_by_user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE reservation_slots (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  seat_id BIGINT UNSIGNED NOT NULL,
  slot_date DATE NOT NULL,
  hour_no TINYINT UNSIGNED NOT NULL,
  slot_status ENUM('active', 'released') NOT NULL DEFAULT 'active',
  active_token TINYINT UNSIGNED NULL DEFAULT 1,
  released_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_reservation_slots_seat_active (seat_id, slot_date, hour_no, active_token),
  UNIQUE KEY uk_reservation_slots_user_active (user_id, slot_date, hour_no, active_token),
  KEY idx_reservation_slots_reservation (reservation_id),
  CONSTRAINT fk_reservation_slots_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id),
  CONSTRAINT fk_reservation_slots_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_reservation_slots_seat FOREIGN KEY (seat_id) REFERENCES seats (id),
  CONSTRAINT chk_reservation_slots_hour CHECK (hour_no BETWEEN 0 AND 23)
) ENGINE=InnoDB;

CREATE TABLE room_checkin_codes (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  study_room_id BIGINT UNSIGNED NOT NULL,
  code_date DATE NOT NULL,
  code_value CHAR(6) NOT NULL,
  qr_payload VARCHAR(255) NULL,
  valid_from DATETIME NOT NULL,
  valid_to DATETIME NOT NULL,
  code_status ENUM('active', 'expired', 'revoked') NOT NULL DEFAULT 'active',
  created_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_room_checkin_codes_room_date (study_room_id, code_date),
  UNIQUE KEY uk_room_checkin_codes_date_value (code_date, code_value),
  KEY idx_room_checkin_codes_validity (study_room_id, valid_from, valid_to),
  CONSTRAINT fk_room_checkin_codes_room FOREIGN KEY (study_room_id) REFERENCES study_rooms (id),
  CONSTRAINT fk_room_checkin_codes_created_by FOREIGN KEY (created_by) REFERENCES users (id),
  CONSTRAINT chk_room_checkin_codes_time CHECK (valid_from < valid_to)
) ENGINE=InnoDB;

CREATE TABLE reservation_checkins (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT UNSIGNED NOT NULL,
  room_checkin_code_id BIGINT UNSIGNED NULL,
  checkin_method ENUM('manual_code', 'qr', 'admin_override') NOT NULL,
  submitted_code VARCHAR(32) NULL,
  device_identifier VARCHAR(128) NULL,
  ip_address VARCHAR(64) NULL,
  checkin_result ENUM('success', 'rejected') NOT NULL DEFAULT 'success',
  result_message VARCHAR(255) NULL,
  checked_in_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_reservation_checkins_reservation (reservation_id),
  KEY idx_reservation_checkins_code (room_checkin_code_id),
  CONSTRAINT fk_reservation_checkins_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id),
  CONSTRAINT fk_reservation_checkins_code FOREIGN KEY (room_checkin_code_id) REFERENCES room_checkin_codes (id)
) ENGINE=InnoDB;

CREATE TABLE violations (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  reservation_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  violation_type ENUM('no_checkin', 'late_cancel', 'manual') NOT NULL,
  violation_status ENUM('active', 'revoked') NOT NULL DEFAULT 'active',
  occurred_at DATETIME NOT NULL,
  points INT UNSIGNED NOT NULL DEFAULT 1,
  description_text VARCHAR(255) NULL,
  revoked_at DATETIME NULL,
  revoked_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_violations_reservation (reservation_id),
  KEY idx_violations_user_status (user_id, violation_status, occurred_at),
  CONSTRAINT fk_violations_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id),
  CONSTRAINT fk_violations_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_violations_revoked_by FOREIGN KEY (revoked_by) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE violation_appeals (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  violation_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  appeal_status ENUM('pending', 'approved', 'rejected', 'withdrawn') NOT NULL DEFAULT 'pending',
  appeal_reason TEXT NOT NULL,
  evidence_json JSON NULL,
  review_note VARCHAR(255) NULL,
  submitted_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  reviewed_at DATETIME NULL,
  reviewed_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_violation_appeals_violation (violation_id),
  KEY idx_violation_appeals_user_status (user_id, appeal_status),
  CONSTRAINT fk_violation_appeals_violation FOREIGN KEY (violation_id) REFERENCES violations (id),
  CONSTRAINT fk_violation_appeals_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_violation_appeals_reviewed_by FOREIGN KEY (reviewed_by) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE violation_action_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  violation_id BIGINT UNSIGNED NOT NULL,
  action_type VARCHAR(32) NOT NULL,
  operator_user_id BIGINT UNSIGNED NULL,
  action_note VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_violation_action_logs_violation (violation_id, created_at),
  CONSTRAINT fk_violation_action_logs_violation FOREIGN KEY (violation_id) REFERENCES violations (id),
  CONSTRAINT fk_violation_action_logs_operator FOREIGN KEY (operator_user_id) REFERENCES users (id)
) ENGINE=InnoDB;

-- =========================================================
-- 5. Notification and AI support domain
-- =========================================================

CREATE TABLE notification_templates (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  template_key VARCHAR(64) NOT NULL,
  channel ENUM('email', 'mini_program', 'in_app', 'sms') NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  language_code VARCHAR(16) NOT NULL DEFAULT 'zh-CN',
  title_template VARCHAR(255) NULL,
  body_template TEXT NOT NULL,
  is_active TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_templates (template_key, channel, language_code)
) ENGINE=InnoDB;

CREATE TABLE notification_jobs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  reservation_id BIGINT UNSIGNED NULL,
  violation_id BIGINT UNSIGNED NULL,
  template_id BIGINT UNSIGNED NULL,
  channel ENUM('email', 'mini_program', 'in_app', 'sms') NOT NULL,
  event_type VARCHAR(64) NOT NULL,
  job_status ENUM('pending', 'processing', 'sent', 'failed', 'cancelled') NOT NULL DEFAULT 'pending',
  scheduled_at DATETIME NOT NULL,
  sent_at DATETIME NULL,
  dedupe_key VARCHAR(128) NULL,
  payload_json JSON NULL,
  failure_reason VARCHAR(255) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_notification_jobs_dedupe (dedupe_key),
  KEY idx_notification_jobs_user_status (user_id, job_status, scheduled_at),
  KEY idx_notification_jobs_reservation (reservation_id),
  CONSTRAINT fk_notification_jobs_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_notification_jobs_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (id),
  CONSTRAINT fk_notification_jobs_violation FOREIGN KEY (violation_id) REFERENCES violations (id),
  CONSTRAINT fk_notification_jobs_template FOREIGN KEY (template_id) REFERENCES notification_templates (id)
) ENGINE=InnoDB;

CREATE TABLE notification_deliveries (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  notification_job_id BIGINT UNSIGNED NOT NULL,
  provider_name VARCHAR(64) NOT NULL,
  provider_message_id VARCHAR(128) NULL,
  delivery_status ENUM('queued', 'sent', 'delivered', 'failed') NOT NULL,
  response_code VARCHAR(64) NULL,
  response_message VARCHAR(255) NULL,
  retry_count SMALLINT UNSIGNED NOT NULL DEFAULT 0,
  delivered_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notification_deliveries_job (notification_job_id, created_at),
  CONSTRAINT fk_notification_deliveries_job FOREIGN KEY (notification_job_id) REFERENCES notification_jobs (id)
) ENGINE=InnoDB;

CREATE TABLE ai_conversations (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  channel ENUM('web', 'mini_program', 'admin') NOT NULL,
  session_token VARCHAR(128) NOT NULL,
  conversation_title VARCHAR(128) NULL,
  conversation_status ENUM('active', 'closed', 'archived') NOT NULL DEFAULT 'active',
  context_json JSON NULL,
  last_message_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_ai_conversations_session (session_token),
  KEY idx_ai_conversations_user_status (user_id, conversation_status),
  CONSTRAINT fk_ai_conversations_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE ai_messages (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  conversation_id BIGINT UNSIGNED NOT NULL,
  message_role ENUM('user', 'assistant', 'system', 'tool') NOT NULL,
  content_text MEDIUMTEXT NOT NULL,
  intent_code VARCHAR(64) NULL,
  referenced_entity_type VARCHAR(32) NULL,
  referenced_entity_id BIGINT UNSIGNED NULL,
  metadata_json JSON NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_ai_messages_conversation (conversation_id, created_at),
  CONSTRAINT fk_ai_messages_conversation FOREIGN KEY (conversation_id) REFERENCES ai_conversations (id)
) ENGINE=InnoDB;

CREATE TABLE ai_intents (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  conversation_id BIGINT UNSIGNED NOT NULL,
  message_id BIGINT UNSIGNED NOT NULL,
  intent_code VARCHAR(64) NOT NULL,
  confidence_score DECIMAL(5,4) NOT NULL,
  extracted_slots_json JSON NULL,
  route_status ENUM('matched_rule', 'llm_enhanced', 'rejected', 'executed') NOT NULL DEFAULT 'matched_rule',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_ai_intents_conversation (conversation_id, created_at),
  CONSTRAINT fk_ai_intents_conversation FOREIGN KEY (conversation_id) REFERENCES ai_conversations (id),
  CONSTRAINT fk_ai_intents_message FOREIGN KEY (message_id) REFERENCES ai_messages (id)
) ENGINE=InnoDB;

CREATE TABLE ai_action_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  conversation_id BIGINT UNSIGNED NOT NULL,
  message_id BIGINT UNSIGNED NULL,
  action_type VARCHAR(64) NOT NULL,
  target_type VARCHAR(32) NULL,
  target_id BIGINT UNSIGNED NULL,
  action_status ENUM('pending', 'success', 'failed', 'ignored') NOT NULL DEFAULT 'pending',
  request_json JSON NULL,
  response_json JSON NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_ai_action_logs_conversation (conversation_id, created_at),
  CONSTRAINT fk_ai_action_logs_conversation FOREIGN KEY (conversation_id) REFERENCES ai_conversations (id),
  CONSTRAINT fk_ai_action_logs_message FOREIGN KEY (message_id) REFERENCES ai_messages (id)
) ENGINE=InnoDB;

-- =========================================================
-- 6. Audit, event, statistics, configuration domain
-- =========================================================

CREATE TABLE audit_logs (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  actor_user_id BIGINT UNSIGNED NULL,
  request_id VARCHAR(64) NULL,
  action_module VARCHAR(64) NOT NULL,
  action_type VARCHAR(64) NOT NULL,
  resource_type VARCHAR(64) NOT NULL,
  resource_id BIGINT UNSIGNED NULL,
  ip_address VARCHAR(64) NULL,
  user_agent VARCHAR(255) NULL,
  before_json JSON NULL,
  after_json JSON NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_audit_logs_actor_time (actor_user_id, created_at),
  KEY idx_audit_logs_request (request_id),
  CONSTRAINT fk_audit_logs_actor FOREIGN KEY (actor_user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE business_events (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  event_type VARCHAR(64) NOT NULL,
  aggregate_type VARCHAR(64) NOT NULL,
  aggregate_id BIGINT UNSIGNED NOT NULL,
  actor_user_id BIGINT UNSIGNED NULL,
  occurred_at DATETIME NOT NULL,
  event_date DATE NOT NULL,
  payload_json JSON NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_business_events_aggregate (aggregate_type, aggregate_id, occurred_at),
  KEY idx_business_events_date_type (event_date, event_type),
  CONSTRAINT fk_business_events_actor FOREIGN KEY (actor_user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE room_daily_stats (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  stats_date DATE NOT NULL,
  study_room_id BIGINT UNSIGNED NOT NULL,
  reservation_count INT UNSIGNED NOT NULL DEFAULT 0,
  active_user_count INT UNSIGNED NOT NULL DEFAULT 0,
  seat_hour_total INT UNSIGNED NOT NULL DEFAULT 0,
  seat_hour_booked INT UNSIGNED NOT NULL DEFAULT 0,
  seat_hour_checked_in INT UNSIGNED NOT NULL DEFAULT 0,
  violation_count INT UNSIGNED NOT NULL DEFAULT 0,
  utilization_rate DECIMAL(8,4) NOT NULL DEFAULT 0.0000,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_room_daily_stats (stats_date, study_room_id),
  CONSTRAINT fk_room_daily_stats_room FOREIGN KEY (study_room_id) REFERENCES study_rooms (id)
) ENGINE=InnoDB;

CREATE TABLE seat_daily_stats (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  stats_date DATE NOT NULL,
  seat_id BIGINT UNSIGNED NOT NULL,
  reservation_count INT UNSIGNED NOT NULL DEFAULT 0,
  occupied_hours INT UNSIGNED NOT NULL DEFAULT 0,
  checked_in_hours INT UNSIGNED NOT NULL DEFAULT 0,
  violation_count INT UNSIGNED NOT NULL DEFAULT 0,
  heat_score DECIMAL(8,4) NOT NULL DEFAULT 0.0000,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_seat_daily_stats (stats_date, seat_id),
  CONSTRAINT fk_seat_daily_stats_seat FOREIGN KEY (seat_id) REFERENCES seats (id)
) ENGINE=InnoDB;

CREATE TABLE user_daily_stats (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  stats_date DATE NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  reservation_count INT UNSIGNED NOT NULL DEFAULT 0,
  checked_in_count INT UNSIGNED NOT NULL DEFAULT 0,
  cancel_count INT UNSIGNED NOT NULL DEFAULT 0,
  violation_count INT UNSIGNED NOT NULL DEFAULT 0,
  last_active_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_daily_stats (stats_date, user_id),
  CONSTRAINT fk_user_daily_stats_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE system_settings (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  setting_key VARCHAR(64) NOT NULL,
  setting_group_name VARCHAR(64) NOT NULL,
  value_type ENUM('string', 'int', 'decimal', 'bool', 'json') NOT NULL,
  value_text VARCHAR(255) NULL,
  value_json JSON NULL,
  description_text VARCHAR(255) NULL,
  is_public TINYINT(1) NOT NULL DEFAULT 0,
  updated_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_system_settings_key (setting_key),
  CONSTRAINT fk_system_settings_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE feature_flags (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  flag_key VARCHAR(64) NOT NULL,
  flag_description VARCHAR(255) NULL,
  is_enabled TINYINT(1) NOT NULL DEFAULT 0,
  config_json JSON NULL,
  updated_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_feature_flags_key (flag_key),
  CONSTRAINT fk_feature_flags_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE dictionary_items (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  dict_type VARCHAR(64) NOT NULL,
  dict_key VARCHAR(64) NOT NULL,
  dict_value VARCHAR(128) NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  is_system TINYINT(1) NOT NULL DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_dictionary_items (dict_type, dict_key)
) ENGINE=InnoDB;

-- =========================================================
-- 7. Notice and feedback domain
-- =========================================================

CREATE TABLE admin_notices (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  title VARCHAR(120) NOT NULL,
  content TEXT NOT NULL,
  notice_type ENUM('system', 'rule', 'event', 'maintenance') NOT NULL DEFAULT 'system',
  notice_status ENUM('draft', 'published') NOT NULL DEFAULT 'draft',
  is_pinned TINYINT(1) NOT NULL DEFAULT 0,
  author_name VARCHAR(64) NULL,
  published_at DATETIME NULL,
  created_by BIGINT UNSIGNED NULL,
  updated_by BIGINT UNSIGNED NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_admin_notices_status_type (notice_status, notice_type),
  KEY idx_admin_notices_pinned_published (is_pinned, published_at),
  CONSTRAINT fk_admin_notices_created_by FOREIGN KEY (created_by) REFERENCES users (id),
  CONSTRAINT fk_admin_notices_updated_by FOREIGN KEY (updated_by) REFERENCES users (id)
) ENGINE=InnoDB;

CREATE TABLE user_feedbacks (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,
  student_name VARCHAR(64) NOT NULL,
  student_no VARCHAR(32) NOT NULL,
  feedback_type ENUM('bug', 'suggestion', 'complaint') NOT NULL,
  title VARCHAR(200) NOT NULL,
  content TEXT NOT NULL,
  feedback_status ENUM('pending', 'processing', 'resolved') NOT NULL DEFAULT 'pending',
  reply_content TEXT NULL,
  replied_by BIGINT UNSIGNED NULL,
  replied_at DATETIME NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (id),
  KEY idx_user_feedbacks_user_status (user_id, feedback_status),
  KEY idx_user_feedbacks_status_type (feedback_status, feedback_type),
  CONSTRAINT fk_user_feedbacks_user FOREIGN KEY (user_id) REFERENCES users (id),
  CONSTRAINT fk_user_feedbacks_replied_by FOREIGN KEY (replied_by) REFERENCES users (id)
) ENGINE=InnoDB;

-- =========================================================
-- 8. Seed data
-- =========================================================

INSERT INTO campuses (campus_code, campus_name, address)
VALUES ('MAIN', '示例大学主校区', '中国示例省示例市大学路 1 号');

INSERT INTO organizations (campus_id, parent_id, org_code, org_name, org_type, org_path, display_order)
VALUES
  ((SELECT id FROM campuses WHERE campus_code = 'MAIN'), NULL, 'SCHOOL', '示例大学', 'school', '/SCHOOL/', 1),
  ((SELECT id FROM campuses WHERE campus_code = 'MAIN'), (SELECT id FROM organizations WHERE org_code = 'SCHOOL'), 'LIB', '图书馆', 'library', '/SCHOOL/LIB/', 10),
  ((SELECT id FROM campuses WHERE campus_code = 'MAIN'), (SELECT id FROM organizations WHERE org_code = 'SCHOOL'), 'CS', '计算机学院', 'college', '/SCHOOL/CS/', 20),
  ((SELECT id FROM campuses WHERE campus_code = 'MAIN'), (SELECT id FROM organizations WHERE org_code = 'SCHOOL'), 'MATH', '数学学院', 'college', '/SCHOOL/MATH/', 30);

INSERT INTO users (
  user_type,
  account_status,
  student_no,
  employee_no,
  full_name,
  nickname,
  email,
  phone,
  primary_organization_id,
  must_change_password,
  remark
) VALUES
  (
    'staff',
    'active',
    NULL,
    'A0001',
    '系统管理员',
    'Admin',
    'admin@study.edu.cn',
    '13800000000',
    (SELECT id FROM organizations WHERE org_code = 'SCHOOL'),
    1,
    '初始系统管理员，首次登录后请立即修改密码。'
  ),
  (
    'staff',
    'active',
    NULL,
    'A0002',
    '计算机学院管理员',
    'CS Admin',
    'cs_admin@study.edu.cn',
    '13800000001',
    (SELECT id FROM organizations WHERE org_code = 'CS'),
    1,
    '院系管理员演示账号。'
  ),
  (
    'student',
    'active',
    '20230001',
    NULL,
    '张三',
    'Zhang San',
    '20230001@study.edu.cn',
    '13900000001',
    (SELECT id FROM organizations WHERE org_code = 'CS'),
    1,
    '学生演示账号。'
  );

-- Password hashes below are placeholders. Replace them with real bcrypt hashes before enabling login.
INSERT INTO auth_identities (
  user_id,
  identity_type,
  identity_key,
  auth_source,
  credential_hash,
  is_verified,
  verified_at
) VALUES
  (
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn'),
    'email_password',
    'admin@study.edu.cn',
    'local',
    '$2b$12$ZTncLC/HXhjUhSPJOrAv/eDu9Xt3fR9Caa5XA/zoPitFaX/9MWa7q',
    1,
    CURRENT_TIMESTAMP
  ),
  (
    (SELECT id FROM users WHERE email = 'cs_admin@study.edu.cn'),
    'email_password',
    'cs_admin@study.edu.cn',
    'local',
    '$2b$12$ZTncLC/HXhjUhSPJOrAv/eDu9Xt3fR9Caa5XA/zoPitFaX/9MWa7q',
    1,
    CURRENT_TIMESTAMP
  ),
  (
    (SELECT id FROM users WHERE student_no = '20230001'),
    'student_no_password',
    '20230001',
    'local',
    '$2b$12$6kNv8z4pJv8UHOiFJa0vY.j8/Q5yQzuIY.aNZ3/scGQNWg8MYpLZS',
    1,
    CURRENT_TIMESTAMP
  ),
  (
    (SELECT id FROM users WHERE student_no = '20230001'),
    'email_password',
    '20230001@study.edu.cn',
    'local',
    '$2b$12$6kNv8z4pJv8UHOiFJa0vY.j8/Q5yQzuIY.aNZ3/scGQNWg8MYpLZS',
    1,
    CURRENT_TIMESTAMP
  );

INSERT INTO user_contact_methods (user_id, contact_type, contact_value, is_primary, is_verified, verified_at)
VALUES
  ((SELECT id FROM users WHERE email = 'admin@study.edu.cn'), 'email', 'admin@study.edu.cn', 1, 1, CURRENT_TIMESTAMP),
  ((SELECT id FROM users WHERE email = 'cs_admin@study.edu.cn'), 'email', 'cs_admin@study.edu.cn', 1, 1, CURRENT_TIMESTAMP),
  ((SELECT id FROM users WHERE student_no = '20230001'), 'email', '20230001@study.edu.cn', 1, 1, CURRENT_TIMESTAMP),
  ((SELECT id FROM users WHERE student_no = '20230001'), 'in_app', 'user:20230001', 0, 1, CURRENT_TIMESTAMP);

INSERT INTO user_notification_preferences (user_id, channel, event_type, is_enabled)
VALUES
  ((SELECT id FROM users WHERE email = 'admin@study.edu.cn'), 'email', 'system_alert', 1),
  ((SELECT id FROM users WHERE email = 'cs_admin@study.edu.cn'), 'email', 'room_event', 1),
  ((SELECT id FROM users WHERE student_no = '20230001'), 'email', 'reservation_created', 1),
  ((SELECT id FROM users WHERE student_no = '20230001'), 'email', 'reservation_reminder', 1),
  ((SELECT id FROM users WHERE student_no = '20230001'), 'email', 'checkin_warning', 1),
  ((SELECT id FROM users WHERE student_no = '20230001'), 'in_app', 'violation_created', 1);

INSERT INTO roles (role_code, role_name, role_description, is_system)
VALUES
  ('SYSTEM_ADMIN', '系统管理员', '拥有全局管理权限，可维护角色、系统参数、全校数据。', 1),
  ('OPERATIONS_ADMIN', '普通管理员', '负责预约、自习室、座位、签到码和违约查看。', 1),
  ('ORG_ADMIN', '院系管理员', '仅管理所属院系范围内的自习室、座位和预约。', 1);

INSERT INTO permissions (permission_code, permission_name, resource_code, action_code, permission_description)
VALUES
  ('reservation.read', '查看预约', 'reservation', 'read', '查看预约记录'),
  ('reservation.manage', '管理预约', 'reservation', 'manage', '创建、修改、取消预约'),
  ('reservation.proxy_book', '代学生预约', 'reservation', 'proxy_book', '管理员代学生预约或取消预约'),
  ('violation.read', '查看违约', 'violation', 'read', '查看违约记录'),
  ('violation.appeal_review', '审核违约申诉', 'violation', 'appeal_review', '审核学生违约申诉'),
  ('room.read', '查看自习室', 'room', 'read', '查看自习室信息'),
  ('room.manage', '管理自习室', 'room', 'manage', '创建、修改、注销自习室'),
  ('seat.read', '查看座位', 'seat', 'read', '查看座位和座位地图'),
  ('seat.manage', '管理座位', 'seat', 'manage', '创建、修改、注销座位'),
  ('seat.maintenance', '维护座位状态', 'seat', 'maintenance', '临时关闭或恢复座位'),
  ('user.read', '查看用户', 'user', 'read', '查看用户列表与详情'),
  ('user.assign_role', '分配角色', 'user', 'assign_role', '为管理员用户分配角色'),
  ('settings.manage', '修改系统配置', 'settings', 'manage', '调整系统参数和开关'),
  ('stats.read', '查看统计报表', 'stats', 'read', '查看利用率和趋势报表'),
  ('checkin_code.manage', '管理签到码', 'checkin_code', 'manage', '查看与下载每日签到码'),
  ('audit.read', '查看审计日志', 'audit', 'read', '查看系统审计日志');

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p
WHERE r.role_code = 'SYSTEM_ADMIN';

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p
WHERE r.role_code = 'OPERATIONS_ADMIN'
  AND p.permission_code IN (
    'reservation.read',
    'reservation.manage',
    'reservation.proxy_book',
    'violation.read',
    'room.read',
    'room.manage',
    'seat.read',
    'seat.manage',
    'seat.maintenance',
    'stats.read',
    'checkin_code.manage'
  );

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p
WHERE r.role_code = 'ORG_ADMIN'
  AND p.permission_code IN (
    'reservation.read',
    'reservation.manage',
    'reservation.proxy_book',
    'violation.read',
    'room.read',
    'room.manage',
    'seat.read',
    'seat.manage',
    'seat.maintenance',
    'stats.read',
    'checkin_code.manage'
  );

INSERT INTO user_roles (user_id, role_id, scope_type, campus_id, organization_id, is_primary, assigned_by)
VALUES
  (
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn'),
    (SELECT id FROM roles WHERE role_code = 'SYSTEM_ADMIN'),
    'global',
    NULL,
    NULL,
    1,
    NULL
  ),
  (
    (SELECT id FROM users WHERE email = 'cs_admin@study.edu.cn'),
    (SELECT id FROM roles WHERE role_code = 'ORG_ADMIN'),
    'organization',
    (SELECT id FROM campuses WHERE campus_code = 'MAIN'),
    (SELECT id FROM organizations WHERE org_code = 'CS'),
    1,
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn')
  );

INSERT INTO buildings (campus_id, organization_id, building_code, building_name, address)
VALUES
  (
    (SELECT id FROM campuses WHERE campus_code = 'MAIN'),
    (SELECT id FROM organizations WHERE org_code = 'LIB'),
    'LIB',
    '主图书馆',
    '主校区图书馆广场 1 号'
  ),
  (
    (SELECT id FROM campuses WHERE campus_code = 'MAIN'),
    (SELECT id FROM organizations WHERE org_code = 'CS'),
    'CSB',
    '计算机学院实验楼',
    '主校区学院路 8 号'
  );

INSERT INTO floors (building_id, floor_code, floor_name, floor_number)
VALUES
  ((SELECT id FROM buildings WHERE building_code = 'LIB'), 'F3', '三层', 3),
  ((SELECT id FROM buildings WHERE building_code = 'CSB'), 'F2', '二层', 2);

INSERT INTO room_types (room_type_code, room_type_name, booking_mode, default_policy_json, description_text)
VALUES
  ('NORMAL', '标准自习室', 'seat', JSON_OBJECT('max_hours', 4, 'allow_group_booking', false), '普通整排座位自习室'),
  ('LIBRARY_READING', '图书馆阅览区', 'seat', JSON_OBJECT('max_hours', 4, 'allow_group_booking', false), '公共阅览区，通常全校可见'),
  ('QUIET_POD', '静音仓', 'seat', JSON_OBJECT('max_hours', 2, 'requires_quiet_mode', true), '适合个人专注学习的特殊房型'),
  ('DISCUSSION_ROOM', '讨论间', 'room', JSON_OBJECT('max_hours', 2, 'allow_group_booking', true), '为未来小组协作扩展预留');

INSERT INTO study_rooms (
  campus_id,
  owner_organization_id,
  building_id,
  floor_id,
  room_type_id,
  room_code,
  room_name,
  display_name,
  visibility_scope,
  room_status,
  location_detail,
  description_text,
  total_capacity,
  open_rule_type,
  map_width,
  map_height,
  policy_json,
  created_by,
  updated_by
) VALUES
  (
    (SELECT id FROM campuses WHERE campus_code = 'MAIN'),
    (SELECT id FROM organizations WHERE org_code = 'LIB'),
    (SELECT id FROM buildings WHERE building_code = 'LIB'),
    (SELECT id FROM floors WHERE building_id = (SELECT id FROM buildings WHERE building_code = 'LIB') AND floor_code = 'F3'),
    (SELECT id FROM room_types WHERE room_type_code = 'LIBRARY_READING'),
    'LIB-301',
    '图书馆三层自习区',
    '主图书馆三层开放阅览区',
    'public',
    'active',
    '图书馆三层东侧',
    '面向全校学生开放，支持座位地图展示。',
    6,
    'weekly_schedule',
    1200,
    800,
    JSON_OBJECT('allow_waitlist', false, 'max_hours', 4),
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn'),
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn')
  ),
  (
    (SELECT id FROM campuses WHERE campus_code = 'MAIN'),
    (SELECT id FROM organizations WHERE org_code = 'CS'),
    (SELECT id FROM buildings WHERE building_code = 'CSB'),
    (SELECT id FROM floors WHERE building_id = (SELECT id FROM buildings WHERE building_code = 'CSB') AND floor_code = 'F2'),
    (SELECT id FROM room_types WHERE room_type_code = 'QUIET_POD'),
    'CS-201',
    '计算机学院静音仓',
    '计算机学院二层静音学习区',
    'organization',
    'active',
    '实验楼二层北侧',
    '院系专属静音仓，为后续特殊教室策略扩展预留。',
    4,
    'weekly_schedule',
    900,
    600,
    JSON_OBJECT('allow_waitlist', false, 'max_hours', 2, 'requires_quiet_mode', true),
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn'),
    (SELECT id FROM users WHERE email = 'cs_admin@study.edu.cn')
  );

INSERT INTO room_visibility_rules (study_room_id, allow_type, organization_id)
VALUES
  (
    (SELECT id FROM study_rooms WHERE room_code = 'CS-201'),
    'organization',
    (SELECT id FROM organizations WHERE org_code = 'CS')
  );

INSERT INTO room_open_rules (study_room_id, rule_kind, weekday_no, opens_at, closes_at, is_open)
VALUES
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'weekly', 0, '07:00:00', '22:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'weekly', 1, '07:00:00', '22:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'weekly', 2, '07:00:00', '22:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'weekly', 3, '07:00:00', '22:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'weekly', 4, '07:00:00', '22:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'weekly', 5, '07:00:00', '22:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'weekly', 6, '07:00:00', '22:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'CS-201'), 'weekly', 0, '08:00:00', '21:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'CS-201'), 'weekly', 1, '08:00:00', '21:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'CS-201'), 'weekly', 2, '08:00:00', '21:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'CS-201'), 'weekly', 3, '08:00:00', '21:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'CS-201'), 'weekly', 4, '08:00:00', '21:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'CS-201'), 'weekly', 5, '08:00:00', '21:00:00', 1),
  ((SELECT id FROM study_rooms WHERE room_code = 'CS-201'), 'weekly', 6, '08:00:00', '21:00:00', 1);

INSERT INTO room_assets (study_room_id, asset_kind, asset_name, asset_url, mime_type, sort_order, metadata_json, created_by)
VALUES
  (
    (SELECT id FROM study_rooms WHERE room_code = 'LIB-301'),
    'floor_plan',
    'LIB-301 Floor Plan',
    '/static/floorplans/lib-301.png',
    'image/png',
    1,
    JSON_OBJECT('width', 1200, 'height', 800),
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn')
  ),
  (
    (SELECT id FROM study_rooms WHERE room_code = 'CS-201'),
    'floor_plan',
    'CS-201 Floor Plan',
    '/static/floorplans/cs-201.png',
    'image/png',
    1,
    JSON_OBJECT('width', 900, 'height', 600),
    (SELECT id FROM users WHERE email = 'cs_admin@study.edu.cn')
  );

INSERT INTO seat_maps (study_room_id, version_no, map_status, background_url, map_width, map_height, published_at, created_by)
VALUES
  (
    (SELECT id FROM study_rooms WHERE room_code = 'LIB-301'),
    1,
    'published',
    '/static/floorplans/lib-301.png',
    1200,
    800,
    CURRENT_TIMESTAMP,
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn')
  ),
  (
    (SELECT id FROM study_rooms WHERE room_code = 'CS-201'),
    1,
    'published',
    '/static/floorplans/cs-201.png',
    900,
    600,
    CURRENT_TIMESTAMP,
    (SELECT id FROM users WHERE email = 'cs_admin@study.edu.cn')
  );

INSERT INTO seats (
  study_room_id,
  seat_code,
  display_label,
  seat_type,
  row_no,
  col_no,
  has_power,
  is_window_side,
  is_accessible,
  seat_status,
  is_bookable,
  map_x,
  map_y,
  map_width,
  map_height,
  map_rotation,
  metadata_json
) VALUES
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'A01', 'A01', 'standard', 1, 1, 1, 1, 0, 'active', 1, 120, 120, 48, 48, 0, JSON_OBJECT('zone', 'window')),
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'A02', 'A02', 'standard', 1, 2, 1, 0, 0, 'active', 1, 220, 120, 48, 48, 0, JSON_OBJECT('zone', 'window')),
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'B01', 'B01', 'standard', 2, 1, 0, 1, 0, 'active', 1, 120, 260, 48, 48, 0, JSON_OBJECT('zone', 'quiet')),
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'B02', 'B02', 'standard', 2, 2, 0, 0, 0, 'active', 1, 220, 260, 48, 48, 0, JSON_OBJECT('zone', 'quiet')),
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'C01', 'C01', 'standard', 3, 1, 1, 0, 1, 'active', 1, 120, 400, 48, 48, 0, JSON_OBJECT('zone', 'accessible')),
  ((SELECT id FROM study_rooms WHERE room_code = 'LIB-301'), 'C02', 'C02', 'standard', 3, 2, 1, 0, 0, 'active', 1, 220, 400, 48, 48, 0, JSON_OBJECT('zone', 'accessible')),
  ((SELECT id FROM study_rooms WHERE room_code = 'CS-201'), 'Q01', 'Q01', 'quiet_pod', 1, 1, 1, 0, 0, 'active', 1, 140, 120, 56, 56, 0, JSON_OBJECT('noise_level', 'low')),
  ((SELECT id FROM study_rooms WHERE room_code = 'CS-201'), 'Q02', 'Q02', 'quiet_pod', 1, 2, 1, 0, 0, 'active', 1, 260, 120, 56, 56, 0, JSON_OBJECT('noise_level', 'low')),
  ((SELECT id FROM study_rooms WHERE room_code = 'CS-201'), 'Q03', 'Q03', 'quiet_pod', 2, 1, 1, 0, 0, 'active', 1, 140, 280, 56, 56, 0, JSON_OBJECT('noise_level', 'very_low')),
  ((SELECT id FROM study_rooms WHERE room_code = 'CS-201'), 'Q04', 'Q04', 'quiet_pod', 2, 2, 1, 0, 0, 'active', 1, 260, 280, 56, 56, 0, JSON_OBJECT('noise_level', 'very_low'));

INSERT INTO seat_features (feature_code, feature_name, value_type)
VALUES
  ('quiet_level', '安静级别', 'text'),
  ('near_socket', '靠近插座排', 'bool'),
  ('natural_light', '自然采光', 'bool');

INSERT INTO seat_feature_values (seat_id, feature_id, text_value)
VALUES
  (
    (SELECT id FROM seats WHERE study_room_id = (SELECT id FROM study_rooms WHERE room_code = 'CS-201') AND seat_code = 'Q01'),
    (SELECT id FROM seat_features WHERE feature_code = 'quiet_level'),
    'high'
  ),
  (
    (SELECT id FROM seats WHERE study_room_id = (SELECT id FROM study_rooms WHERE room_code = 'CS-201') AND seat_code = 'Q03'),
    (SELECT id FROM seat_features WHERE feature_code = 'quiet_level'),
    'very_high'
  );

INSERT INTO seat_feature_values (seat_id, feature_id, bool_value)
VALUES
  (
    (SELECT id FROM seats WHERE study_room_id = (SELECT id FROM study_rooms WHERE room_code = 'LIB-301') AND seat_code = 'A01'),
    (SELECT id FROM seat_features WHERE feature_code = 'natural_light'),
    1
  ),
  (
    (SELECT id FROM seats WHERE study_room_id = (SELECT id FROM study_rooms WHERE room_code = 'LIB-301') AND seat_code = 'A02'),
    (SELECT id FROM seat_features WHERE feature_code = 'near_socket'),
    1
  );

INSERT INTO seat_map_items (seat_map_id, item_type, item_key, item_label, x, y, width, height, rotation_angle, style_json)
VALUES
  (
    (SELECT id FROM seat_maps WHERE study_room_id = (SELECT id FROM study_rooms WHERE room_code = 'LIB-301') AND version_no = 1),
    'label',
    'entry',
    '入口',
    40,
    700,
    80,
    36,
    0,
    JSON_OBJECT('color', '#2b6cb0')
  ),
  (
    (SELECT id FROM seat_maps WHERE study_room_id = (SELECT id FROM study_rooms WHERE room_code = 'CS-201') AND version_no = 1),
    'facility',
    'screen',
    '签到屏',
    720,
    80,
    100,
    40,
    0,
    JSON_OBJECT('color', '#2f855a')
  );

INSERT INTO room_checkin_codes (study_room_id, code_date, code_value, qr_payload, valid_from, valid_to, code_status, created_by)
VALUES
  (
    (SELECT id FROM study_rooms WHERE room_code = 'LIB-301'),
    CURRENT_DATE,
    'LIB301',
    'room:LIB-301|date:CURRENT|code:LIB301',
    CONCAT(CURRENT_DATE, ' 00:00:00'),
    CONCAT(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), ' 00:00:00'),
    'active',
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn')
  ),
  (
    (SELECT id FROM study_rooms WHERE room_code = 'CS-201'),
    CURRENT_DATE,
    'CS201A',
    'room:CS-201|date:CURRENT|code:CS201A',
    CONCAT(CURRENT_DATE, ' 00:00:00'),
    CONCAT(DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), ' 00:00:00'),
    'active',
    (SELECT id FROM users WHERE email = 'cs_admin@study.edu.cn')
  );

INSERT INTO notification_templates (template_key, channel, event_type, title_template, body_template, is_active)
VALUES
  ('reservation_created', 'email', 'reservation_created', '预约成功通知', '您已成功预约 {{room_name}} 的 {{seat_code}}，时间为 {{start_at}} - {{end_at}}。', 1),
  ('reservation_reminder', 'email', 'reservation_reminder', '预约开始提醒', '您的预约将在 {{start_at}} 开始，请及时前往 {{room_name}} 的 {{seat_code}}。', 1),
  ('checkin_warning', 'email', 'checkin_warning', '签到提醒', '您的预约已开始但尚未签到，请在截止前完成签到。', 1),
  ('violation_created', 'in_app', 'violation_created', '违约通知', '您的预约因未签到已记为违约，请在个人中心查看详情。', 1),
  ('appeal_result', 'email', 'appeal_result', '申诉处理结果', '您的违约申诉结果为：{{appeal_status}}。', 1);

INSERT INTO system_settings (setting_key, setting_group_name, value_type, value_text, description_text, is_public, updated_by)
VALUES
  ('reservation.max_hours', 'reservation', 'int', '4', '单次预约最大时长（小时）', 1, (SELECT id FROM users WHERE email = 'admin@study.edu.cn')),
  ('reservation.slot_granularity_minutes', 'reservation', 'int', '60', '预约时间粒度（分钟）', 1, (SELECT id FROM users WHERE email = 'admin@study.edu.cn')),
  ('reservation.free_cancel_before_minutes', 'reservation', 'int', '30', '预约开始前多少分钟内取消不记违约', 1, (SELECT id FROM users WHERE email = 'admin@study.edu.cn')),
  ('checkin.remind_before_minutes', 'checkin', 'int', '15', '预约开始前提醒时间（分钟）', 1, (SELECT id FROM users WHERE email = 'admin@study.edu.cn')),
  ('checkin.warning_after_start_minutes', 'checkin', 'int', '10', '开始后首次未签到提醒时间（分钟）', 1, (SELECT id FROM users WHERE email = 'admin@study.edu.cn')),
  ('checkin.deadline_after_start_minutes', 'checkin', 'int', '15', '开始后自动违约截止时间（分钟）', 1, (SELECT id FROM users WHERE email = 'admin@study.edu.cn')),
  ('notification.email_enabled', 'notification', 'bool', 'true', '是否启用邮件通知', 0, (SELECT id FROM users WHERE email = 'admin@study.edu.cn')),
  ('assistant.enabled', 'assistant', 'bool', 'true', '是否启用智能助手', 0, (SELECT id FROM users WHERE email = 'admin@study.edu.cn'));

INSERT INTO feature_flags (flag_key, flag_description, is_enabled, config_json, updated_by)
VALUES
  ('assistant-llm-enabled', '启用大语言模型增强回复', 0, JSON_OBJECT('provider', 'anthropic', 'fallback', 'rules'), (SELECT id FROM users WHERE email = 'admin@study.edu.cn')),
  ('violation-appeal-enabled', '启用学生违约申诉流程', 1, JSON_OBJECT('review_sla_hours', 48), (SELECT id FROM users WHERE email = 'admin@study.edu.cn')),
  ('special-room-policy-enabled', '启用特殊房型差异化预约策略', 1, JSON_OBJECT('supported_types', JSON_ARRAY('QUIET_POD', 'DISCUSSION_ROOM')), (SELECT id FROM users WHERE email = 'admin@study.edu.cn'));

INSERT INTO admin_notices (title, content, notice_type, notice_status, is_pinned, author_name, published_at, created_by)
VALUES
  (
    '图书馆自习室暑期开放时间调整通知',
    '为保障同学们暑期学习，图书馆自习室开放时间调整为每日 08:00-22:00，请同学们合理安排时间，按时预约入座。如有疑问请联系图书馆管理处。',
    'system',
    'published',
    1,
    '系统管理员',
    '2026-04-01 10:00:00',
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn')
  ),
  (
    '关于违约处理规则更新的说明',
    '自2026年4月15日起，累计违约3次的用户将被暂停预约权限7天。请各位同学遵守预约规则，按时签到或提前取消预约。',
    'rule',
    'published',
    1,
    '系统管理员',
    '2026-04-05 09:00:00',
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn')
  ),
  (
    '五一假期自习室开放安排',
    '五一假期（5月1日至5月5日）期间，各校区自习室正常开放，开放时间为 08:00-21:00。假期结束后恢复正常时间。',
    'event',
    'published',
    0,
    '计算机学院管理员',
    '2026-04-20 11:00:00',
    (SELECT id FROM users WHERE email = 'cs_admin@study.edu.cn')
  ),
  (
    '枫林校区医学院自习室开放公告',
    '枫林校区医学院专用自习室（ZH-MED-301）已完成装修，即将向医学院同学开放预约，请关注后续通知。',
    'system',
    'draft',
    0,
    '计算机学院管理员',
    NULL,
    (SELECT id FROM users WHERE email = 'cs_admin@study.edu.cn')
  ),
  (
    '系统维护通知：4月30日凌晨停服1小时',
    '为保障系统稳定运行，将于2026年4月30日凌晨02:00-03:00进行例行系统维护，期间预约功能暂停使用，请提前做好预约安排。',
    'maintenance',
    'published',
    0,
    '系统管理员',
    '2026-04-28 14:00:00',
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn')
  );

INSERT INTO user_feedbacks (user_id, student_name, student_no, feedback_type, title, content, feedback_status, reply_content, replied_by, replied_at)
VALUES
  (
    (SELECT id FROM users WHERE student_no = '20230001'),
    '张三',
    '20230001',
    'bug',
    '签到页面无法正常显示二维码',
    '在签到时点击"扫码签到"按钮后，页面空白，无法显示二维码。使用的是 iOS 微信内置浏览器，已尝试重启无效。',
    'resolved',
    '感谢反馈！该问题已在最新版本中修复，微信浏览器签到功能已恢复正常，请更新后重试。',
    (SELECT id FROM users WHERE email = 'admin@study.edu.cn'),
    '2026-04-07 10:00:00'
  ),
  (
    (SELECT id FROM users WHERE student_no = '20230001'),
    '张三',
    '20230001',
    'suggestion',
    '建议增加"常用座位"收藏功能',
    '每次预约都要重新查找座位比较麻烦，建议增加收藏常用座位的功能，下次可以快速预约。',
    'pending',
    NULL,
    NULL,
    NULL
  ),
  (
    (SELECT id FROM users WHERE student_no = '20230001'),
    '张三',
    '20230001',
    'complaint',
    '图书馆B区座位标注有误',
    '图书馆B区B12座位在系统里显示有插座，但实际去了发现没有，影响了我的学习计划，希望尽快核实修正。',
    'processing',
    NULL,
    NULL,
    NULL
  );

INSERT INTO system_settings (setting_key, setting_group_name, value_type, value_text, description_text, is_public, updated_by)
VALUES
  ('violation.max_count', 'violation', 'int', '3', '触发自动封号的违约次数阈值', 1, (SELECT id FROM users WHERE email = 'admin@study.edu.cn')),
  ('violation.suspend_days', 'violation', 'int', '7', '违规封号持续天数', 1, (SELECT id FROM users WHERE email = 'admin@study.edu.cn')),
  ('reservation.allow_weekend', 'reservation', 'bool', 'true', '是否允许周末预约', 1, (SELECT id FROM users WHERE email = 'admin@study.edu.cn'));

INSERT INTO dictionary_items (dict_type, dict_key, dict_value, sort_order)
VALUES
  ('room_visibility_scope', 'public', '全校可见', 1),
  ('room_visibility_scope', 'organization', '组织可见', 2),
  ('room_visibility_scope', 'custom', '自定义可见', 3),
  ('reservation_status', 'pending_checkin', '待签到', 1),
  ('reservation_status', 'checked_in', '已签到', 2),
  ('reservation_status', 'cancelled', '已取消', 3),
  ('reservation_status', 'violated', '已违约', 4),
  ('reservation_status', 'completed', '已完成', 5),
  ('violation_type', 'no_checkin', '超时未签到', 1),
  ('violation_type', 'late_cancel', '临时取消', 2),
  ('room_type', 'QUIET_POD', '静音仓', 10),
  ('room_type', 'DISCUSSION_ROOM', '讨论间', 20);