-- Chỉ thêm khi chưa có (chạy lại nhiều lần không bị trùng; tương thích cả PostgreSQL và H2)
INSERT INTO medicines (id, name, stock, price)
SELECT 'MED-001', 'Paracetamol 500mg', 100, 1500 WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE id = 'MED-001');
INSERT INTO medicines (id, name, stock, price)
SELECT 'MED-002', 'Amoxicillin 250mg', 50, 3000 WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE id = 'MED-002');
INSERT INTO medicines (id, name, stock, price)
SELECT 'MED-003', 'Vitamin C 1000mg', 80, 2500 WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE id = 'MED-003');
INSERT INTO medicines (id, name, stock, price)
SELECT 'MED-004', 'Berberin', 10, 1000 WHERE NOT EXISTS (SELECT 1 FROM medicines WHERE id = 'MED-004');
