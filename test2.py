import datetime

times = ["02:00", "03:59", "04:01", "08:01", "17:00", "22:00"]
today = datetime.datetime.now().replace(second=0, microsecond=0)

for t in times:
    hr, mn = map(int, t.split(':'))
    current = today.replace(hour=hr, minute=mn)
    
    start = current.replace(hour=21, minute=0)
    end = current.replace(hour=4, minute=0)
    
    if end <= start:
        now_minutes = hr * 60 + mn
        end_minutes = 4 * 60
        
        if now_minutes < end_minutes:
            start -= datetime.timedelta(days=1)
        else:
            end += datetime.timedelta(days=1)
            
    isCurrent = (current >= start) and (current < end)
    isPast = (current >= end)
    print(f"Time {t} -> Past: {isPast}, Current: {isCurrent}, Start: {start.strftime('%d %H:%M')}, End: {end.strftime('%d %H:%M')}")
