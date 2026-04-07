import urllib.request
import json
import datetime

url = 'https://script.google.com/macros/s/AKfycbx2EmeSjsbcD_bGTZQBmG7xwhUBEdvjL33k4GqqcH8lv-b4mmzzjAOtZt7FwQksVvhF/exec'
timestamp = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")

data = {
    "name": "Jules Test Run Python",
    "email": "jules.test.py@example.com",
    "phone": "+15550000000",
    "uid": "test-uid-jules",
    "verifiedStatus": "test_run",
    "timestamp": timestamp
}

req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'))
req.add_header('Content-Type', 'application/json')

try:
    response = urllib.request.urlopen(req)
    print("Status code:", response.getcode())
    print("SUCCESS! Login test run is successful and connected to the sheet.")
except Exception as e:
    print("Failed:", str(e))
