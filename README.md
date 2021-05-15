# BRChain Admin Server :: Hyperledger Fabric 관리자 서버

Hyperledger Fabric 네트워크를 docker 컨테이너 기반으로 쉽게 구성하고 테스트를 해볼수있는 관리자 서비스 입니다

[`swaager-server link`](http://34.64.205.180:8080/swagger-ui.html#/)

# Skill-set

* Java
* Spring boot
* Fabric Sdk
* JPA
* MariaDB
* WebSocket
* JWT
* Docker

# BRChain Admin Server API

## index /api
- /auth
    - [`POST /register`](#POST-apiauthregister)
    - [`POST /login`](#POST-apiauthlogin)
    - [`POST /refresh`](#POST-apiauthrefresh)
    - [`POST /logout`](#POST-apiauthlogout)
- /core
    - [`GET /block`](#GET-apicoreblock)
    - [`GET /block/list`](#GET-apicoreblocklist)
    - [`GET /member/list`](#GET-apicorememberlist)
    - [`POST /org/create`](#POST-apicoreorgcreate)
    - [`GET /org/list`](#GET-apicoreorglist)
    - [`GET /transaction`](#GET-apicoretransaction)
    - [`GET /transaction/list`](#GET-apicoretransactionlist)
    - /channel
        - [`POST /create`](#POST-apicorechannelupdateanchor)
        - [`GET /event/register`](#GET-apicorechanneleventregister)
        - [`GET /event/unregister`](#GET-apicorechanneleventunregister)
        - [`GET /list`](#GET-apicorechannellist)
        - [`GET /list/peer`](#GET-apicorechannellistpeer)
        - [`GET /list/summary`](#GET-apicorechannellistsummary)
        - [`GET /update/anchor`](#GET-apicorechannelupdateanchor)
    - /chaincode
        - [`POST /active`](#POST-apicorechaincodeactive)
        - [`POST /install`](#POST-apicorechaincodeinstall)
        - [`GET /list`](#GET-apicorechaincodelist)
        - [`GET /list/channel`](#GET-coreauthchaincodelistchannel)
        - [`GET /list/summary`](#GET-apicorechaincodelistsummary)
        - [`GET /list/toactive`](#GET-coreauthchaincodelisttoactive)
        - [`POST /upload`](#POST-apicorechaincodeupload)
    - /container
        - [`GET /check/port`](#GET-apicorecontainercheckport)
        - [`GET /list`](#GET-apicorecontainerlist)
        - [`GET /remove`](#GET-apicorecontainerremove)
        

## Status Code
 - 0xxx success
   - 0000, success
 - 9xxx server error(http 5xx)
    - 901X
      - 9010, docker connection error
    - 902x
      - 9020, wallet create error
    - 903x
      - 9030, channel create error
      - 9031, channel join error
    - 904x
      - 9040, fabric context error
      - 9041, fabric client error
      - 9042, fabric query error
    - 905x
      - 9050, get channel config error
      - 9051, update channel config error
    - 906x
      - 9060, chaincode install error
      - 9061, chaincode package error
      - 9062, chaincode upload error
    - 907x
      - 9070, file upload error
      - 9071, file download error
      - 9072, delete dir error
      - 9073, exec command error
    - 908X
      - 9080, jwt error
    - 909x
      - 9020, thread error
 - 8xxx  server error(http 4xx)
    - 801X peer error
      - 8010, docker already anchor peer error
    - 802X event error
      - 8020, already registered listener error
    - 803x login error
      - 8030, invalid refresh_token
      - 8031, invalid jwt
      - 8032, invalid password
      - 8033, account not found

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

## `GET /api/core/block`
블록 데이터 해쉬값으로 블록 정보를 조회하는 API

### request

```json
request params

{
    "blockDataHash": "조회할 블록 데이터 해쉬값" 

}
```

### response

- on success

```json
{
    "resultCode": "0000",
    "resultMessage": "Success get block by channel name",
    "resultData": {            
            "blockDataHash": "데이터 해쉬값",
            "blockNum": "블록 번호",
            "txCount": "트랜잭션 개수",
            "timestamp": "YYYY-MM-DD HH:MI:SS",
            "prevDataHash": "이전 데이터 해쉬값",
            "channelInfoDto": 채널정보json,            
            "createdAt": "YYYY-MM-DD HH:MI:SS"
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


## `GET /api/core/block/list`
HyperLedger Fabric 채널 이름으로 블록 정보들을 조회하는 API

### request

```json
request params

{
    "channelName": "조회할 HyperLedger Fabric 조직명" 

}
```

### response

- on success

```json
{	
    "resultCode": "0000",
    "resultMessage": "Success get block by channel name",
    "resultData": [
        {
            "blockDataHash": "블록 데이터 해쉬",
            "blockNum;": "블록 번호",
            "txCount;": "트랜잭션 개수",
            "timestamp": "타임스탬프",
            "prevDataHash": "이전블록 데이터 해쉬",
            "txList": "트랜잭션 개수"
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

## `GET /api/core/transaction`
트랜잭션 아이디값으로 트랜잭션 정보를 조회하는 API

### request

```json
request params

{
    "txId": "트랜잭션 아이디값" 

}
```

### response

- on success

```json
{	
    "resultCode": "0000",
    "resultMessage": "Success get tx by tx id",
    "resultData": {
        "createdAt": "YYYY-MM-DD HH:MI:SS",
        "modifiedAt": "YYYY-MM-DD HH:MI:SS",
        "id": "pk",
        "txId": "트랜잭션 아이디",
        "creatorId": "트랜잭션 생성자 msp아이디",
        "txType": "트랜잭션 타입",
        "timestamp": "YYYY-MM-DD HH:MI:SS",
        "ccName": "체인코드 이름",
        "ccVersion": "체인코드 버전",
        "ccArgs": "체인코드 파라미터",
        "blockEntity": 블록 정보json,  
        "channelInfoEntity": 채널 정보json  
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


## `GET /api/core/transaction/list`
HyperLedger Fabric 채널 이름으로 트렌잭션 정보들을 조회하는 API

### request

```json
request params

{
    "channelName": "조회할 HyperLedger Fabric 채널명" 

}
```

### response

- on success

```json
{	
    "resultCode": "0000",
    "resultMessage": "Success get tx by channel name",
    "resultData": [
        {
        "createdAt": "YYYY-MM-DD HH:MI:SS",
        "modifiedAt": "YYYY-MM-DD HH:MI:SS",
        "id": "pk",
        "txId": "트랜잭션 아이디",
        "creatorId": "트랜잭션 생성자 msp아이디",
        "txType": "트랜잭션 타입",
        "timestamp": "YYYY-MM-DD HH:MI:SS",
        "ccName": "체인코드 이름",
        "ccVersion": "체인코드 버전",
        "ccArgs": "체인코드 파라미터",
        "blockEntity": 블록 정보json,  
        "channelInfoEntity": 채널 정보json  
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


## `GET /api/core/channel/list/summary`
Hyperledger Fabric 채널 요약 리스트를 조회하는 API
### response

```

- on success 

```json
{
  "resultCode": "0000",
  "resultMessage": "Success get channel info by channel name",
  "resultData": [
    {
        "channelName": "채널 이름",
        "channelBlock": "채널 블럭수",
        "channelTx": "채널 트랜잭션수",
        "preBlockCnt": "지난달 채널 블럭수",
        "nowBlockCnt": "이번달 채널 트랜잭션수",
        "preTxCnt": "지난달 채널 트랜잭션수",
        "nowTxCnt": "이번달 채널 트랜잭션수",
        "percent": "트랜잭션 증감율",
        "flag": "증가감소 플래그"
    },
  ]
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

## `POST /api/core/chaincode/active`
Hyperledger Fabric 체인코드를 채널에 활성화 하는 API
### request

```json
request body


{
  "ccLang": "체인코드 언어",
  "ccName": "체인코드 이름",
  "ccVersion": "체인코드 버전",
  "channelName": "활성화 대상 채널",
  "id": "체인코드 아이디"
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

## `GET /api/core/chaincode/list/summary`
Hyperledger Fabric 체인코드 요약 리스트를 조회하는 API
### response

- on success 

```json
{
  "resultCode": "0000",
  "resultMessage": "Success get cc summary",
  "resultData": {
	"conName": "컨테이너 이름".
	"ccCnt":   "체인코드 개수"
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

## `GET /api/core/chaincode/list/toactive`
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



## `GET /api/core/container/check/port`
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



## `GET /api/core/container/remove`
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