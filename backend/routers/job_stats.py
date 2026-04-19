from fastapi import APIRouter
from services.analytics_service import AnalyticsService

# initializing the router
router = APIRouter(prefix="/api/v1/analytics", tags=["analytics"])

# initializing the service
service = AnalyticsService()

@router.get("/market-share")
def read_marker_share():
    return service.get_market_share_stats()

@router.get("/seniority")
def read_seniority():
    return service.get_seniority_distribution()

@router.get("/daily-trends")
def daily_trends():
    return service.get_daily_trends()


@router.get("/top_companies")
def top_companies():
    return service.get_top_companies()