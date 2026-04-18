from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import psycopg2
from routers import job_stats
from dotenv import load_dotenv
import os

app = FastAPI()

# critical - allowing the frontend to talk to this API
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],
    allow_methods=["*"],
    allow_headers=["*"],
)


# registering the router
app.include_router(job_stats.router)

@app.get("/")
def root():
    return {"mesage":"lanka job tracker api is alive"}