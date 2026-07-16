import datetime

times = ["02:00", "03:59", "04:01", "08:01", "17:00", "22:00"]
today = datetime.datetime.now().replace(second=0, microsecond=0)

for t in times:
    hr, mn = map(int, t.split(':'))
    current = today.replace(hour=hr, minute=mn)
    
    start = current.replace(hour=21, minute=0)
    end = current.replace(hour=4, minute=0)
    
    if end <= start:
        cutoff = end + datetime.timedelta(hours=4)
        if current < cutoff:
            start -= datetime.timedelta(days=1)
        else:
            end += datetime.timedelta(days=1)
            
    isCurrent = (current >= start) and (current < end)
    isPast = (current >= end)
    print(f"Time {t} -> Past: {isPast}, Current: {isCurrent}")
