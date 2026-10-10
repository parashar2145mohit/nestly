CREATE TABLE society (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(160) NOT NULL,
  address VARCHAR(500) NOT NULL,
  subscription_plan VARCHAR(30) NOT NULL DEFAULT 'TRIAL',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE block (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  block_name VARCHAR(80) NOT NULL,
  CONSTRAINT fk_block_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT uq_block_name UNIQUE (society_id, block_name),
  INDEX idx_block_society (society_id)
);

CREATE TABLE flat (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  block_id BIGINT NOT NULL,
  flat_number VARCHAR(30) NOT NULL,
  flat_type VARCHAR(30) NOT NULL,
  CONSTRAINT fk_flat_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_flat_block FOREIGN KEY (block_id) REFERENCES block(id),
  CONSTRAINT uq_flat_number UNIQUE (block_id, flat_number),
  INDEX idx_flat_society (society_id)
);

CREATE TABLE app_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NULL,
  role ENUM('SUPER_ADMIN','SOCIETY_ADMIN','RESIDENT','SECURITY_GUARD') NOT NULL,
  name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  phone VARCHAR(20),
  password_hash VARCHAR(255) NOT NULL,
  enabled BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_society FOREIGN KEY (society_id) REFERENCES society(id),
  INDEX idx_user_society (society_id)
);

CREATE TABLE resident (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL UNIQUE,
  flat_id BIGINT NOT NULL,
  is_owner BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_resident_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_resident_user FOREIGN KEY (user_id) REFERENCES app_user(id),
  CONSTRAINT fk_resident_flat FOREIGN KEY (flat_id) REFERENCES flat(id),
  INDEX idx_resident_society (society_id)
);

CREATE TABLE complaint (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  resident_id BIGINT NOT NULL,
  assigned_to BIGINT,
  description TEXT NOT NULL,
  category ENUM('PLUMBING','ELECTRICAL','SECURITY','CLEANING','OTHER') NOT NULL DEFAULT 'OTHER',
  priority ENUM('HIGH','MEDIUM','LOW') NOT NULL DEFAULT 'LOW',
  ai_confidence DECIMAL(5,4),
  status ENUM('OPEN','IN_PROGRESS','RESOLVED') NOT NULL DEFAULT 'OPEN',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resolved_at TIMESTAMP NULL,
  CONSTRAINT fk_complaint_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_complaint_resident FOREIGN KEY (resident_id) REFERENCES resident(id),
  CONSTRAINT fk_complaint_assignee FOREIGN KEY (assigned_to) REFERENCES app_user(id),
  INDEX idx_complaint_tenant_status (society_id, status)
);

CREATE TABLE complaint_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  complaint_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  body TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_comment_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_comment_complaint FOREIGN KEY (complaint_id) REFERENCES complaint(id) ON DELETE CASCADE,
  CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES app_user(id)
);

CREATE TABLE maintenance_invoice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  flat_id BIGINT NOT NULL,
  billing_month DATE NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  due_date DATE NOT NULL,
  status ENUM('PENDING','PARTIAL','PAID','OVERDUE') NOT NULL DEFAULT 'PENDING',
  generated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_invoice_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_invoice_flat FOREIGN KEY (flat_id) REFERENCES flat(id),
  CONSTRAINT uq_invoice_cycle UNIQUE (flat_id, billing_month),
  INDEX idx_invoice_tenant_status (society_id, status)
);

CREATE TABLE payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  invoice_id BIGINT NOT NULL,
  amount_paid DECIMAL(12,2) NOT NULL,
  payment_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  method VARCHAR(30) NOT NULL,
  txn_id VARCHAR(100) NOT NULL UNIQUE,
  CONSTRAINT fk_payment_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_payment_invoice FOREIGN KEY (invoice_id) REFERENCES maintenance_invoice(id),
  INDEX idx_payment_society (society_id)
);

CREATE TABLE visitor (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  contact VARCHAR(20),
  photo_url VARCHAR(500),
  entry_time TIMESTAMP NULL,
  exit_time TIMESTAMP NULL,
  flat_id BIGINT NOT NULL,
  approved_by_resident BIGINT,
  qr_token VARCHAR(100) UNIQUE,
  valid_from TIMESTAMP NULL,
  valid_until TIMESTAMP NULL,
  CONSTRAINT fk_visitor_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_visitor_flat FOREIGN KEY (flat_id) REFERENCES flat(id),
  CONSTRAINT fk_visitor_approver FOREIGN KEY (approved_by_resident) REFERENCES resident(id),
  INDEX idx_visitor_tenant_entry (society_id, entry_time)
);

CREATE TABLE notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  title VARCHAR(200) NOT NULL,
  body TEXT NOT NULL,
  posted_by BIGINT NOT NULL,
  posted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notice_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_notice_author FOREIGN KEY (posted_by) REFERENCES app_user(id),
  INDEX idx_notice_society_date (society_id, posted_at)
);

CREATE TABLE amenity (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  capacity INT NOT NULL,
  CONSTRAINT fk_amenity_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT uq_amenity_name UNIQUE (society_id, name)
);

CREATE TABLE amenity_booking (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  amenity_id BIGINT NOT NULL,
  flat_id BIGINT NOT NULL,
  slot_start TIMESTAMP NOT NULL,
  slot_end TIMESTAMP NOT NULL,
  status ENUM('CONFIRMED','CANCELLED') NOT NULL DEFAULT 'CONFIRMED',
  CONSTRAINT fk_booking_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_booking_amenity FOREIGN KEY (amenity_id) REFERENCES amenity(id),
  CONSTRAINT fk_booking_flat FOREIGN KEY (flat_id) REFERENCES flat(id),
  INDEX idx_booking_conflict (amenity_id, slot_start, slot_end)
);

CREATE TABLE expense (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  category VARCHAR(80) NOT NULL,
  description VARCHAR(500) NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  receipt_url VARCHAR(500),
  logged_by BIGINT NOT NULL,
  logged_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  anomaly_flag BOOLEAN NOT NULL DEFAULT FALSE,
  anomaly_score DECIMAL(6,5),
  CONSTRAINT fk_expense_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_expense_user FOREIGN KEY (logged_by) REFERENCES app_user(id),
  INDEX idx_expense_tenant_category (society_id, category)
);

CREATE TABLE poll (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  question VARCHAR(500) NOT NULL,
  options_json JSON NOT NULL,
  closes_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_poll_society FOREIGN KEY (society_id) REFERENCES society(id)
);

CREATE TABLE poll_vote (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  poll_id BIGINT NOT NULL,
  flat_id BIGINT NOT NULL,
  selected_option VARCHAR(200) NOT NULL,
  voted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_vote_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_vote_poll FOREIGN KEY (poll_id) REFERENCES poll(id),
  CONSTRAINT fk_vote_flat FOREIGN KEY (flat_id) REFERENCES flat(id),
  CONSTRAINT uq_vote_per_flat UNIQUE (poll_id, flat_id)
);

CREATE TABLE parking_slot (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  slot_number VARCHAR(30) NOT NULL,
  flat_id BIGINT NULL,
  visitor_slot BOOLEAN NOT NULL DEFAULT FALSE,
  occupied BOOLEAN NOT NULL DEFAULT FALSE,
  CONSTRAINT fk_parking_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_parking_flat FOREIGN KEY (flat_id) REFERENCES flat(id),
  CONSTRAINT uq_parking_slot UNIQUE (society_id, slot_number)
);

CREATE TABLE staff_member (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  name VARCHAR(120) NOT NULL,
  staff_type ENUM('MAID','DRIVER','COOK','DELIVERY','OTHER') NOT NULL,
  phone VARCHAR(20),
  photo_url VARCHAR(500),
  id_proof_url VARCHAR(500),
  qr_token VARCHAR(100) NOT NULL UNIQUE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT fk_staff_society FOREIGN KEY (society_id) REFERENCES society(id)
);

CREATE TABLE staff_entry_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  staff_id BIGINT NOT NULL,
  entry_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  exit_time TIMESTAMP NULL,
  CONSTRAINT fk_staff_log_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_staff_log_member FOREIGN KEY (staff_id) REFERENCES staff_member(id)
);

CREATE TABLE sos_alert (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  resident_id BIGINT NOT NULL,
  latitude DECIMAL(10,7),
  longitude DECIMAL(10,7),
  status ENUM('ACTIVE','ACKNOWLEDGED','RESOLVED') NOT NULL DEFAULT 'ACTIVE',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  acknowledged_at TIMESTAMP NULL,
  CONSTRAINT fk_sos_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_sos_resident FOREIGN KEY (resident_id) REFERENCES resident(id),
  INDEX idx_sos_active (society_id, status)
);

CREATE TABLE payment_risk_score (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  society_id BIGINT NOT NULL,
  resident_id BIGINT NOT NULL,
  risk_score DECIMAL(5,4) NOT NULL,
  calculated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_risk_society FOREIGN KEY (society_id) REFERENCES society(id),
  CONSTRAINT fk_risk_resident FOREIGN KEY (resident_id) REFERENCES resident(id),
  INDEX idx_risk_tenant_score (society_id, risk_score)
);
