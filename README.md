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
    - [`POST /register`](#POST-apiauthregister)
    - [`POST /login`](#POST-apiauthlogin)
    - [`POST /refresh`](#POST-apiauthrefresh)
    - [`POST /logout`](#POST-apiauthlogout)
- /core
    - [`GET /container/list`](#GET-apicorecontainerlist)
    - [`GET /org/list`](#GET-apicoreorglist)
    - [`POST /org/create`](#POST-apicoreorgcreate)
    - [`GET /member/list`](#GET-apicorememberlist)
    - [`GET /remove`](#GET-apicoreremove)
    - [`GET /check/port`](#GET-apicorecheckport)
    - /channel
        - [`GET /list`](#GET-apicorechannellist)
        - [`GET /list/peer`](#GET-apicorechannellistpeer)
        - [`POST /create`](#POST-apicorechannelcreate)
        - [`GET /event/register`](#GET-apicorechanneleventregister)
        - [`GET /event/unregister`](#GET-apicorechanneleventunregister)
        - [`GET /update/anchor`](#GET-apicorechannelupdateanchor)
    - /chaincode
        - [`GET /list`](#GET-apicorechaincodelist)
        - [`GET /list/channel`](#GET-coreauthchaincodelistchannel)
        - [`GET /active`](#GET-apicorechaincodeactive)
        - [`POST /install`](#POST-apicorechaincodeinstall)
        - [`POST /upload`](#POST-apicorechaincodeupload)
        - [`POST /active`](#POST-apicorechaincodeactive)
        


## `POST /api/auth/register`
회원가입 API
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
    "resultMessage": "Success register",
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

## `POST /api/auth/login`
로그인 API
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
    "resultMessage": "Success login",
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
JWT 토큰 제발급 API
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
    "resultMessage": "Success refresh token",
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
로그아웃 API
### request

```json
request body

{
    "refreshToken": "f85bbfc5-75f1-4b0b-8777-b887e7b9af2e"
}
```

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "Success logout user",
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

## `GET /api/core/container/list`
모든 도커 컨테이너 정보를 조회하는 API

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

## `GET /api/core/org/list`
조직 타입에 따른 컨테이너 정보를 조회하는 API

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
## `POST /api/core/org/create`
HyperLedger Fabric 조직을 생성하는 API
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

## `GET /api/core/member/list`
HyperLedger Fabric 조직이름에 따른 컨테이너 정보를 조회하는 API
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
컨테이너 ID 또는 조직명으로 컨테이너 중지 및 삭제하는 API
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

## `GET /api/core/check/port`
사용중인 포트인지 체크하는 API
### request

```json
request params
{
    "port": "1111"
}
```

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "사용가능",
    "resultData": null,
    "resultFlag": true
}
```
- on failure

```json
{
    "resultCode": "9999",
    "resultMessage": "사용불가",
    "resultData": null,
    "resultFlag": false
}
```

## `GET /api/core/channel/list`
Hyperledger Fabric 채널 조회하는 API
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
        "orderingOrg": "testOrderer"
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
컨테이너 이름 및 채널명으로 Hyperledger Fabric 채널에 가입된 컨테이너를 조회하는 API
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
HyperLedger Fabric 채널을 생성하고 가입하는 API
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

## `GET /api/core/channel/event/register`
Hyperledger Fabric 채널 이벤트 리스너 등록하는 API
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
    "resultMessage": "Success register block event listener",
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

## `GET /api/core/channel/event/unregister`
Hyperledger Fabric 채널 이벤트 리스너 삭제하는 API
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
    "resultMessage": "Success unregister block event listener",
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

## `GET /api/core/channel/update/anchor`
Success update anchor
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
Hyperledger Fabric 체인코드를 조회하는 API (분리예정)
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
Hyperledger Fabric 채널에 활성화된 체인코드를 조회하는 API
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
Hyperledger Fabric 활성 가능한 체인코드를 조회하는 API
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
Hyperledger Fabric 체인코드를 피어에 설치하는 API
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