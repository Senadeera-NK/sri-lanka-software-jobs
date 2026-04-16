from pydantic import BaseModel
from typing import List, Optional

class JobBase(BaseModel):
    id: int
    title: str
    company: str
    source_name: str
    job_level: Optional[str]=None
    