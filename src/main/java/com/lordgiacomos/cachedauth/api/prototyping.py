import requests
import ujson
import ujson as json
import os

# Yes, I am writing oauth testing in python. While the language is bad for implementation
# of applications (due to slowness from how it handles types), it is useful for rapid prototyping & testing
# because of how it can handle types.


URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode"
CLIENT_ID = os.getenv("CLIENT_ID")
SCOPES = "user.read"


def msaDeviceCode():
    params = {
        "client_id": CLIENT_ID,
        "scope": SCOPES
    }
    headers = {"Content-Type": "x-www-form-urlencoded"}
    req = requests.get(URL, params=params, headers=headers)
    print(req.status_code)
    return req.json()

def postmanPollForAuth(msaInfo):
    url = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token"

    payload = 'grant_type=urn%3Aietf%3Aparams%3Aoauth%3Agrant-type%3Adevice_code&client_id=' +CLIENT_ID + '&device_code=' + msaInfo["device_code"]
    headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
    }

    response = requests.request("POST", url, headers=headers, data=payload)

    print(response.text)



def pollForAuth(msaInfo):
    result = None;
    pollUri = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token"
    #pollUri = "https://l_gs.requestcatcher.com/"
    params = {
        "grant_type": "urn:ietf:params:oauth:grant-type:device_code",
        "client_id": CLIENT_ID,
        "device_code": msaInfo["device_code"]
    }
    #           + "?grant_type=urn:ietf:params:oauth:grant-type:device_code&clientid="
    #           + CLIENT_ID
    #           + "&device_code="
    #           + msaInfo["device_code"]
    #           )
    print(pollUri)
    headers = {"Content-Type": "application/x-www-form-urlencoded"}

    #params2 = ""
    #for k, v in params.items():
    #    if params2 != "":
    #        params2 = params2 + "&"
    #    params2 = params2 + k + "=" + v

    req = requests.post(pollUri, headers=headers, data=params)
    print(req.request.body)
    print(req.status_code)
    print(req.content)
    #print(json.dumps(json.loads(req.content), indent=2))


def test():
    response = msaDeviceCode()
    pollForAuth(response)
    #postmanPollForAuth(response)
    #print(ujson.dumps(response, indent=2))


test()
