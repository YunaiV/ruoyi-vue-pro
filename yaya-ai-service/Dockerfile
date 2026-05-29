FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

COPY pyproject.toml ./
COPY app ./app
COPY yaya_ai ./yaya_ai

RUN pip install --no-cache-dir --upgrade pip \
  && pip install --no-cache-dir .

ENV YAYA_INTERNAL_KEY=local-internal-key

EXPOSE 18080

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "18080"]
