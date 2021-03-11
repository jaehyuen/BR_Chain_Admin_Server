# BRChain Admin Server :: Hyperledger Fabric 관리자 서버

Hyperledger Fabric 네트워크를 쉽게 구성하고 테스트를 해볼수있는 관리자 서비스 입니다

# Skill-set

* Java
* Spring boot
* Fabric Sdk
* JPA
* MariaDB
* WebSocket
* JWT

# BRChain Admin Server API

## index /api
- /auth
    - [`POST /register`](#`POST-apiauthregister`)
    - [`POST /login`](#`POST-apiauthlogin`)
    - [`POST /refresh`](#`POST-apiauthrefresh`)
    - [`POST /logout`](#`POST-apiauthlogout`)
- /core
    - [`GET /containers`](#`GET-apicorecontainers`)
    - [`GET /orgs`](#`GET-apicoreorgs`)
    - [`GET /members`](#`GET-apicoremembers`)
    - [`GET /remove`](#`GET-apicoreremove`)
    - [`GET /check/port`](#`GET-apicorecheckport`)
    - [`POST /create/org`](#`POST-apicorecreateorg`)
    - /channel
        - [`GET /list`](#`GET-apicorechannellist`)
        - [`GET /list/peer`](#`GET-apicorechannellistpeer`)
        - [`POST /create`](#`POST-apicorechannelcreate`)
        - [`GET /register`](#`GET-apicorechannelregister`)
        - [`GET /unregister`](#`GET-apicorechannelunregister`)
        - [`GET /anchor`](#`GET-apicorechannelanchor`)
    - /chaincode
        - [`GET /list`](#`GET-apicorechaincodelist`)
        - [`GET /list/channel`](#`GET-coreauthchaincodelistchannel`)
        - [`GET /active`](#`GET-apicorechaincodeactive`)
        - [`POST /install`](#`POST-apicorechaincodeinstall`)
        - [`POST /upload`](#`POST-apicorechaincodeupload`)
        - [`POST /active`](#`POST-apicorechaincodeactive`)
        


## `POST /api/auth/register`

### request

```json
request body

{
    "userName": "test",
    "userId": "testid",
    "userPassword": "Asdf!234",
    "userEmail": "email@example.com"
}
```

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "Success Register",
    "resultData": "",
    "resultFlag": true
}
```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `POST /api/auth/login`

### request

```json
request body

{
    "userId": "testid",
    "userPassword": "Asdf!234"
}
```

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "Success Login",
    "resultData": {
        "accessToken": "access token value",
        "refreshToken": "refresh token value",
        "expiresAt": "2021-03-09T07:35:24.088Z",
        "userId": "testid"
    },
    "resultFlag": true
}
```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `POST /api/auth/refresh`

### request

```json
request body

{
    "userId": "testid",
    "refreshToken": "f85bbfc5-75f1-4b0b-8777-b887e7b9af2e"
}
```

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "Success Refresh Token",
    "resultData": {
        "accessToken": "access token value",
        "refreshToken": "refresh token value",
        "expiresAt": "2021-03-09T07:35:24.088Z",
        "userId": "testid"
    },
    "resultFlag": true
}
```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `POST /api/auth/logout`

### request

```json
request body

{
    "userId": "testid",
    "refreshToken": "f85bbfc5-75f1-4b0b-8777-b887e7b9af2e"
}
```

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "Success Logout User",
    "resultData": "",
    "resultFlag": true
}
```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/containers`

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "Success get all containers info",
    "resultData": [
        {
            "conId": "container id",
            "conName": "container name",
            "conCreated": "container created time",
            "conStatus": "container status"

        }
    ],
    "resultFlag": true
}
```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/orgs`

### request

```json
request params

{
    "type": "peer" 

}
```

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "Success get all containers info",
    "resultData": [
        {
            "orgName": "testOrg",
            "orgType": "peer",
            "conCnt": 1

        }
    ],
    "resultFlag": true
}
```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/members`

### request

```json
request params

{
    "orgName": "testOrg"
}
```

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "Success get testOrg member info list",
    "resultData": [
        {
            "orgName": "testOrg",
            "orgType": "peer",
            "conNum": "1",
            "conName": "peer1.orgtestOrg.com",
            "conPort": 1111

        }
    ],
    "resultFlag": true
}
```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/remove`

### request

```json
request params
{
    "conId": "122dqwd12q1wd12...." or null,
    "orgName": "testOrg" or null
}
```

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "Success remove container",
    "resultData": null,
    "resultFlag": true
}
```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `POST /api/core/create/org`

### request

```json
request body 

[
    {
        "orgName": "testOrg",
        "orgType": "peer",
        "conType": "ca",
        "conPort": "1111",
        "conCnt": "1"
    },
    {
        "orgName": "testOrg",
        "orgType": "peer",
        "conType": "peer",
        "conPort": "1112",
        "conCnt": "1"
    }
]
```

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "Success create org",
    "resultData": null,
    "resultFlag": true
}
```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/channel/list`

### request

```json
request params 

{
    "channelName": "test-channel" or null
}
```

### response

- on success (channelName != null)

```json
{
    "resultCode": "0000",
    "resultMessage": "Success get channel info",
    "resultData": {
	    "channelBlock": 1,
		"channelTx": 1,
		"channelName": "test-channel",
		"orderingOrg": "testOrdere",
		"appAdminPolicyType": "ImplicitMeta",
		"appAdminPolicyValue": "ANY Admins",
		"channelAdminPolicyType": "ImplicitMeta",
		"channelAdminPolicyValue": "ANY Admins",
		"ordererAdminPolicyType": "ImplicitMeta",
		"ordererAdminPolicyValue": "ANY Admins",
		"batchTimeout": "1s",
		"batchSizeAbsolMax": 81920,
		"batchSizeMaxMsg": 20,
		"batchSizePreferMax": 20480
},
    "resultFlag": true
}
```

- on success (channelName == null)

```json
{
    "resultCode": "0000",
    "resultMessage": "Success get channel info list",
    "resultData": [
    {
        "channelBlock": 1,
        "channelTx": 1,
        "channelName": "test-channel",
        "orderingOrg": "testOrdere"
    },
    {
        "channelBlock": 1,
        "channelTx": 1,
        "channelName": "test-channel2",
        "orderingOrg": "testOrdere"
    }
],
    "resultFlag": true
}

```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/channel/list/peer`

### request

```json
request params 

{
    "conName": "container name" or null,
    "channelName": "test-channel" or null
}
```

### response

- on success (conName != null)

```json
{
    "resultCode": "0000",
    "resultMessage": "Success get channel info",
    "resultData": {
        "channelName": "test-channel",
        "anchorYn": true
    },
    "resultFlag": true
}
```

- on success (channelName != null)

```json
{
    "resultCode": "0000",
    "resultMessage": "Success get channel info by channel name",
    "resultData": {
        "channelName": "test-channel",
        "anchorYn": true
    },
    "resultFlag": true
}

```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `POST /api/core/channel/create`

### request

```json
request body

{
    "channelName": "test-channel",
    "peerOrgs": ["testOrg", "testOrg2"],
    "orderingOrg": "testOrderer",
    "anchorPeerSetting": null
}
```

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "Success create channel",
    "resultData": null,
    "resultFlag": true
}
```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/channel/register`

### request

```json
request params 

{
    "channelName": "test-channel" 
}
```

### response

- on success 

```json
{
    "resultCode": "0000",
    "resultMessage": "Success Register Block EventListener",
    "resultData": null,
    "resultFlag": true
}
```

- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/channel/unregister`

### request

```json
request params 

{
    "channelName": "test-channel" 
}
```

### response

- on success 

```json
{
    "resultCode": "0000",
    "resultMessage": "Success Unregister Block EventListener",
    "resultData": null,
    "resultFlag": true
}
```

- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/channel/unregister`

### request

```json
request params 

{
    "conName": "peer1.orgtestOrg.com",
    "channelName": "test-channel"  
}
```

### response

- on success 

```json
{
    "resultCode": "0000",
    "resultMessage": "Success update anchor",
    "resultData": null,
    "resultFlag": true
}
```

- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/chaincode/list`

### request

```json
request params 

{
    "conName": "peer1.orgtestOrg.com" or null
}
```

### response

- on success (conName != null)

```json
{
    "resultCode": "0000",
    "resultMessage": "Success get chaincode info",
    "resultData": [
        {
            "ccName": "testCc",
            "ccVersion": "1",
            "ccLang": "golang"
        }
    ],
    "resultFlag": true
}
```

- on success (conName == null)

```json
{
    "resultCode": "0000",
    "resultMessage": "Success get chaincode info",
    "resultData": [
        {
            "id":1,
            "ccName": "testCc",
            "ccPath": "/home/test",
            "ccLang": "golang",
            "ccDesc": "chaincode ㄷxplanation",
            "ccVersion": "1"
        }
    ],
    "resultFlag": true
}
```

- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/chaincode/list/channel`

### request

```json
request params 

{
    "channelName": "test-channel"
}
```

### response

- on success 

```json
{
    "resultCode": "0000",
    "resultMessage": "Success get actived chaincode list channel",
    "resultData": [
        {
            "ccName": "testCc",
            "ccVersion": "1",
            "ccLang": "golang"
        }
    ],
    "resultFlag": true
}
```

- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/chaincode/active`

### request

```json
request params 

{
    "channelName": "test-channel"
}
```

### response

- on success 

```json
{
    "resultCode": "0000",
    "resultMessage": "Success get chaincode list channel",
    "resultData": [
        {
            "ccName": "testCc",
            "ccVersion": "1",
            "ccLang": "golang"
        }
    ],
    "resultFlag": true
}
```

- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `POST /api/core/chaincode/install`

### request

```json
request body 

{
    "orgName": "test-channel",
    "conNum": 1,
    "ccName": "testCc",
    "ccVersion": "1"
}
```

### response

- on success 

```json
{
    "resultCode": "0000",
    "resultMessage": "Success install chaincode",
    "resultData": null,
    "resultFlag": true
}
```


- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `POST /api/core/chaincode/upload`

### request

```json
request body 

{
    "ccFile": file,
    "ccName": "testCc",
    "ccDesc": "this is test cc",
    "ccLang": "golang",
    "ccVersion": "1"
}
```

### response

- on success 

```json
{
    "resultCode": "0000",
    "resultMessage": "Success chaincode file upload",
    "resultData": null,
    "resultFlag": true
}
```


- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```

## `POST /api/core/chaincode/active`

### request

```json
request body 

{
    "orgName": "test-channel",
    "ccLang": "golang",
    "ccName": "testCc",
    "ccVersion": "1"
}
```

### response

- on success 

```json
{
    "resultCode": "0000",
    "resultMessage": "Success instantiate chaincode",
    "resultData": null,
    "resultFlag": true
}
```


- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "error messages",
    "resultData": null,
    "resultFlag": false
}
```