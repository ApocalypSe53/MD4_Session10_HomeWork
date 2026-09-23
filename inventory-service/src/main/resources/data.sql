INSERT INTO medicines (id, name, stock, price) VALUES
    ('MED-001', 'Paracetamol 500mg', 100, 1500),
    ('MED-002', 'Amoxicillin 250mg',  50, 3000),
    ('MED-003', 'Vitamin C 1000mg',   80, 2500),
    ('MED-004', 'Berberin',           10, 1000)
ON CONFLICT (id) DO NOTHING;
