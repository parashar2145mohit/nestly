from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_health():
    assert client.get("/health").json() == {"status": "healthy"}

def test_complaint_classifier():
    result = client.post("/ai/complaint/classify", json={"text": "sparks coming from the electrical switch"})
    assert result.status_code == 200
    assert result.json()["category"] == "Electrical"

def test_payment_risk_is_bounded():
    result = client.post("/ai/payment/risk-score", json={"past_delays_days":[5, 12],"total_cycles":6,"defaults_count":1,"outstanding_amount":2000})
    assert 0 <= result.json()["risk_score"] <= 1
