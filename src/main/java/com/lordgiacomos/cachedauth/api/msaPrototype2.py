import requests
import ujson as json
import os
import time


def load_secrets():
    with open("secrets.json") as f:
        return json.load(f)

def save_secrets(amended):
    with open("secrets.json", "w") as f:
        json.dump(amended, f, indent=2)



def msaDeviceCode():
    params = {
        "client_id": CLIENT_ID,
        "scope": SCOPES
    }
    headers = {"Content-Type": "application/x-www-form-urlencoded"}
    req = requests.get("https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode", params=params, headers=headers)
    print(req.status_code)
    return req.json()

def refreshToken(refresh_token):
    url = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token"
    payload = "grant_type=refresh_token&client_id=" + CLIENT_ID + "&refresh_token=" + refresh_token
    headers = {"Content-Type": "application/x-www-form-urlencoded"}
    response = requests.request("POST", url, headers=headers, data=payload)
    print(response.status_code)
    return response.json()

def pollAuthOnce(msaInfo):
    url = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token"

    payload = 'grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Adevice_code&client_id=' + CLIENT_ID + '&device_code=' + msaInfo["device_code"]
    headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
    }

    response = requests.request("POST", url, headers=headers, data=payload)
    return response.status_code, response.json()

def pollLoop(msaInfo):
    for i in range(0, msaInfo["expires_in"], msaInfo["interval"]):
        poll = pollAuthOnce(msaInfo)
        if poll[0] == 200:
            print(200)
            return poll[1]
        else:
            time.sleep(msaInfo["interval"])

def pollXboxLive(accessToken):
    headers = {
        "Content-Type": "application/json",
        "Accept": "application/json"
    }
    payload = {
        "Properties": {
            "AuthMethod": "RPS",
            "SiteName": "user.auth.xboxlive.com",
            "RpsTicket": "d=" + accessToken
        },
        "RelyingParty": "http://auth.xboxlive.com",
        "TokenType": "JWT"
    }
    response = requests.request("POST", "https://user.auth.xboxlive.com/user/authenticate", headers=headers, json=payload)
    print(response.status_code)
    #print(response.content)
    #print(response.




secrets = load_secrets()

CLIENT_ID = secrets["CLIENT_ID"]
SCOPES = "XBoxLive.signin offline_access"


if secrets["REFRESH_TOKEN"] != "":
    # do refresh token flow
    msaAuth = refreshToken(secrets["REFRESH_TOKEN"])
else:
    msaInfo = msaDeviceCode()
    print(msaInfo["user_code"])
    msaAuth = pollLoop(msaInfo)
print(msaAuth)
secrets["ACCESS_TOKEN"] = msaAuth["access_token"]
secrets["REFRESH_TOKEN"] = msaAuth["refresh_token"]
save_secrets(secrets)
pollXboxLive(msaAuth["access_token"])