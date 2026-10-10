from fastapi import FastAPI, UploadFile, File
from pydantic import BaseModel, Field
from sklearn.feature_extraction.text import TfidfVectorizer
from sklearn.linear_model import LogisticRegression
from sklearn.pipeline import Pipeline
from sklearn.ensemble import IsolationForest
import numpy as np

app = FastAPI(title="Nestly AI Service", version="0.1.0")

class Health(BaseModel):
    status: str

@app.get("/health", response_model=Health)
def health() -> Health:
    return Health(status="healthy")

training = [
    ("water leaking from bathroom pipe", "Plumbing", "Medium"),
    ("sewage overflow and terrible smell", "Plumbing", "High"),
    ("tap is dripping in kitchen", "Plumbing", "Low"),
    ("sparks coming from switch board", "Electrical", "High"),
    ("corridor light is not working", "Electrical", "Low"),
    ("power keeps tripping in our flat", "Electrical", "Medium"),
    ("unknown person trying to enter", "Security", "High"),
    ("gate left open during night", "Security", "High"),
    ("camera near lift is offline", "Security", "Medium"),
    ("garbage has not been collected", "Cleaning", "Medium"),
    ("stairs need sweeping", "Cleaning", "Low"),
    ("broken glass in play area", "Cleaning", "High"),
    ("lift is making noise", "Other", "Medium"),
    ("parking line needs repainting", "Other", "Low"),
]
texts = [row[0] for row in training]
category_model = Pipeline([("tfidf", TfidfVectorizer(ngram_range=(1, 2))), ("clf", LogisticRegression(max_iter=500))]).fit(texts, [r[1] for r in training])
priority_model = Pipeline([("tfidf", TfidfVectorizer(ngram_range=(1, 2))), ("clf", LogisticRegression(max_iter=500))]).fit(texts, [r[2] for r in training])

class ComplaintInput(BaseModel):
    text: str = Field(min_length=5, max_length=5000)

class ComplaintResult(BaseModel):
    category: str
    priority: str
    confidence: float

@app.post("/ai/complaint/classify", response_model=ComplaintResult)
def classify_complaint(body: ComplaintInput) -> ComplaintResult:
    category = category_model.predict([body.text])[0]
    priority = priority_model.predict([body.text])[0]
    confidence = float(max(category_model.predict_proba([body.text])[0]))
    return ComplaintResult(category=category, priority=priority, confidence=round(confidence, 4))

class PaymentHistory(BaseModel):
    past_delays_days: list[int] = Field(default_factory=list)
    total_cycles: int = Field(ge=0)
    defaults_count: int = Field(ge=0)
    outstanding_amount: float = Field(ge=0)

@app.post("/ai/payment/risk-score")
def payment_risk(body: PaymentHistory):
    avg_delay = sum(body.past_delays_days) / max(len(body.past_delays_days), 1)
    default_rate = body.defaults_count / max(body.total_cycles, 1)
    z = -2.2 + .07 * avg_delay + 3.4 * default_rate + min(body.outstanding_amount / 50000, 2)
    score = 1 / (1 + np.exp(-z))
    return {"risk_score": round(float(score), 4), "risk_level": "High" if score >= .7 else "Medium" if score >= .4 else "Low"}

class ExpenseInput(BaseModel):
    amount: float = Field(gt=0)
    category_history: list[float] = Field(default_factory=list)

@app.post("/ai/expense/anomaly-check")
def expense_anomaly(body: ExpenseInput):
    history = body.category_history
    if len(history) < 5:
        return {"anomaly": False, "score": 0.0, "reason": "Insufficient category history"}
    model = IsolationForest(contamination=.1, random_state=42).fit(np.array(history).reshape(-1, 1))
    score = float(-model.decision_function([[body.amount]])[0])
    return {"anomaly": bool(model.predict([[body.amount]])[0] == -1), "score": round(score, 4)}

@app.post("/ai/visitor/face-match")
async def face_match(image: UploadFile = File(...)):
    await image.read()
    return {"matched_visitor_id": None, "confidence": 0.0, "status": "No enrolled gallery match"}

class ChatInput(BaseModel):
    resident_id: int
    message: str = Field(min_length=1, max_length=2000)

@app.post("/ai/chat")
def resident_chat(body: ChatInput):
    return {"answer": "I can help with Nestly notices, maintenance, visitors, and complaints. Connect the backend facts endpoint to enable resident-specific answers.", "grounded": False}
