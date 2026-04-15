from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import psycopg2
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

# Load environment variables from .env
load_dotenv()

# Fetch variables
DATABASE_URL = os.getenv("DATABASE_URL")

# Connect to the database
connection = psycopg2.connect(DATABASE_URL)

if(connection):
    print("database connected successfully")


# jobs stats
@app.get("/api/jobs-stats")
def get_stats():
    return{
        "labels":["XpressJobs","TopJobs","ITPro","Rooster"],
        "data":[53, 227, 44, 5]
    }