<script>
	import provinceList from './json/area_province.js';
	import cityList from './json/area_city.js';
	import districtList from './json/area_district.js';
	export default {
		globalData: {
			serverUrl: "http://127.0.0.1:1000",		// 开发环境，不能使用localhost
			// serverUrl: "http://localhost",					// 错误示范！！！
			// serverUrl: "http://192.168.1.6",					// 生产环境
			
			chatServerUrl: "ws://127.0.0.1:875/ws",
			
			deviceCode: 2,
			
			minNode: {}, 
			
			provinceList: provinceList,
			cityList: cityList,
			districtList: districtList,
			
			env: "nlizzard",
			
			// 整个聊天的websocket对象
			CHAT: null,
			// 用于标记当前的聊天服务有没有连接上
			chatSocketOpen: false,
			// ws 状态
			wsStatus: "closed", // closed / connecting / open
		
			// 是否用户主动关闭，比如退出登录
			manualClose: false,
		
			// 重连相关
			reconnectTimer: null,
			reconnectTimes: 0,
			maxReconnectInterval: 30 * 1000,
		
			// 心跳相关
			heartBeatTimer: null,
			heartBeatInterval: 30 * 1000,
		},
		data() {
			return {
			}
		},
		onLaunch: function() {
			console.log('App Launch');
			
			// 1. 获取系统信息
			const appInstance = this; 
			    
			    // 1. 获取系统信息
			    const systemInfo = uni.getSystemInfoSync();
			    
			    // 2. 初始化默认值为移动端
			    appInstance.globalData.deviceCode = 2;
			    
			    // 3. 根据平台和设备类型条件进行判断
			    // #ifdef H5
			    if (systemInfo.deviceType === 'pc') {
			        appInstance.globalData.deviceCode = 1; // PC端
			    } else if (systemInfo.deviceType === 'pad') {
			        appInstance.globalData.deviceCode = 3; // 平板
			    }
			    // #endif
			    
			    // #ifdef APP-PLUS || MP
			    if (systemInfo.deviceType === 'pad') {
			        appInstance.globalData.deviceCode = 3; // 平板
			    } else if (systemInfo.windowWidth > 768) { 
			        // 【修改 2】小程序/App 端 systemInfo.deviceBrand === 'PC' 不稳妥，改为屏幕宽度判断
			        appInstance.globalData.deviceCode = 1; // PC端
			    }
			    // #endif
			    
			    console.log('当前计算出的设备类型码为:', appInstance.globalData.deviceCode);

		
			// 生产环境不要启动就清登录态
			// this.clearUserInfo();
		
			this.getWSServerLink()
				.catch(() => {
					console.log("获取 ws 节点失败，后续使用默认地址或等待重连");
				})
				.finally(() => {
					if (this.userIsLogin()) {
						this.doConnect(false);
					}
				});
		
			// #ifdef APP-PLUS
			plus.screen.lockOrientation("portrait-primary");
			// #endif
		},
		onShow: function() {
			console.log('App Show:' + this.getAppEnv());
		
			if (this.userIsLogin() && !this.globalData.chatSocketOpen) {
				this.scheduleReconnect(0);
			}
		},
		onHide: function() {
			console.log('App Hide')
		},
		methods: {
			
			getWSServerLink() {
				var me = this;
				var serverUrl = me.globalData.serverUrl;
			
				return new Promise((resolve, reject) => {
					uni.request({
						method: "POST",
						url: serverUrl + "/chat/getNettyOnlineInfo",
						success(result) {
							if (result.data && result.data.status == 200 && result.data.data) {
								var minNode = result.data.data;
			
								// 生产环境如果 HTTP 是 https，这里应该使用 wss
								var wsProtocol = serverUrl.indexOf("https://") === 0 ? "wss" : "ws";
			
								me.globalData.chatServerUrl = wsProtocol + "://" + minNode.ip + ":" + minNode.port + "/ws";
								me.globalData.minNode = minNode;
			
								console.log("当前 ws 地址：", me.globalData.chatServerUrl);
			
								resolve(minNode);
							} else {
								reject(result);
							}
						},
						fail(error) {
							reject(error);
						}
					});
				});
			},
			handleSocketDisconnected(socketTask) {
				var gd = this.globalData;
			
				if (gd.CHAT !== socketTask) {
					return;
				}
			
				gd.chatSocketOpen = false;
				gd.wsStatus = "closed";
				gd.CHAT = null;
			
				this.stopHeartBeat();
			
				if (!gd.manualClose) {
					this.scheduleReconnect();
				}
			},
			scheduleReconnect(delay) {
				var me = this;
				var gd = me.globalData;
			
				if (gd.manualClose) {
					return;
				}
			
				if (!me.userIsLogin()) {
					return;
				}
			
				if (gd.chatSocketOpen || gd.wsStatus === "connecting") {
					return;
				}
			
				if (gd.reconnectTimer != null) {
					return;
				}
			
				var wait = delay;
			
				if (wait == null || wait == undefined) {
					var base = 1000;
					wait = Math.min(base * Math.pow(2, gd.reconnectTimes), gd.maxReconnectInterval);
				}
			
				gd.reconnectTimes++;
			
				console.log("准备重连 ws，等待毫秒：", wait);
			
				gd.reconnectTimer = setTimeout(function() {
					gd.reconnectTimer = null;
			
					if (gd.chatSocketOpen || gd.wsStatus === "connecting") {
						return;
					}
			
					me.getWSServerLink()
						.catch(() => {
							console.log("重连前获取 ws 节点失败，尝试使用旧地址重连");
						})
						.finally(() => {
							me.doConnect(true);
						});
			
				}, wait);
			},
			
			clearReconnectTimer() {
				if (this.globalData.reconnectTimer != null) {
					clearTimeout(this.globalData.reconnectTimer);
					this.globalData.reconnectTimer = null;
				}
			},
			
			/**
			 * @param {Object} isReConect 是否重连连接，true-是；false-否
			 */
			doConnect(isReconnect) {
				var me = this;
				var gd = me.globalData;
			
				if (gd.wsStatus === "connecting" || gd.chatSocketOpen) {
					return;
				}
			
				var userInfo = me.getUserInfoSession();
				var userToken = me.getUserSessionToken();
				var userTokenKey = me.getUserSessionTokenKey();
			
				if (userInfo == null || me.isStrEmpty(userToken)) {
					console.log("用户未登录，不连接 ws");
					return;
				}
			
				if (isReconnect) {
					uni.showToast({
						icon: "loading",
						title: "断线重连中...",
						duration: 1000
					});
				}
			
				gd.manualClose = false;
				gd.wsStatus = "connecting";
				gd.chatSocketOpen = false;
			
				me.clearReconnectTimer();
				me.stopHeartBeat();
			
				// 关闭旧连接，避免多个 socket 并存
				if (gd.CHAT != null) {
					try {
						gd.CHAT.close({
							code: 1000,
							reason: "replace old socket"
						});
					} catch (e) {}
					gd.CHAT = null;
				}
			
				var socketTask = uni.connectSocket({
					url: gd.chatServerUrl,
			
					// App / 部分小程序支持 header。
					// H5 WebSocket 通常不能自定义 header，H5 场景可以改成 query 或 init 消息鉴权。
					header: {
						headerUserTokenKey: userTokenKey,
						headerUserToken: userToken
					},
			
					complete: () => {}
				});
			
				gd.CHAT = socketTask;
			
				socketTask.onOpen(function() {
					// 避免旧 socket 的回调污染新 socket 状态
					if (gd.CHAT !== socketTask) {
						return;
					}
			
					gd.wsStatus = "open";
					gd.chatSocketOpen = true;
					gd.reconnectTimes = 0;
			
					console.log("ws连接已打开：", gd.chatServerUrl);
			
					me.sendConnectInitMsg();
					me.startHeartBeat();
				});
			
				socketTask.onClose(function(res) {
					if (gd.CHAT !== socketTask) {
						return;
					}
			
					console.log("ws连接已关闭：", res);
			
					me.handleSocketDisconnected(socketTask);
				});
			
				socketTask.onError(function(error) {
					if (gd.CHAT !== socketTask) {
						return;
					}
			
					console.log("ws连接异常：", error);
			
					try {
						socketTask.close({
							code: 1000,
							reason: "socket error"
						});
					} catch (e) {}
			
					me.handleSocketDisconnected(socketTask);
				});
			
				socketTask.onMessage(function(res) {
					if (gd.CHAT !== socketTask) {
						return;
					}
			
					let msgJSON = null;
			
					try {
						msgJSON = JSON.parse(res.data);
					} catch (e) {
						console.log("ws消息 JSON 解析失败：", res.data);
						return;
					}
			
					// 如果你服务端有 pong，可以在这里单独处理
					// if (msgJSON.chatMsg && msgJSON.chatMsg.msgType == 6) {
					// 	return;
					// }
			
					me.dealReceiveLastestMsg(msgJSON);
				});
			},
			
			sendConnectInitMsg() {
				var me = this;
				var userInfo = me.getUserInfoSession();
				if (userInfo == null || userInfo == "" || userInfo == undefined) {
					return;
				}
				var chatMsg = {
					senderId: userInfo.id,
					msgType: 0
				}
				var dataContent = {
					chatMsg: chatMsg,
					serverNode: me.globalData.minNode
				}
				var msgPending = JSON.stringify(dataContent);
				
				me.globalData.CHAT.send({
					data: msgPending
				});
			},
			
			startHeartBeat() {
				var me = this;
				me.stopHeartBeat();
				me.globalData.heartBeatTimer = setInterval(function() {
					me.sendHeartBeat();
				}, me.globalData.heartBeatInterval);
			},
			
			stopHeartBeat() {
				if (this.globalData.heartBeatTimer != null) {
					clearInterval(this.globalData.heartBeatTimer);
					this.globalData.heartBeatTimer = null;
				}
			},
			
			sendHeartBeat() {
				var me = this;
				var gd = me.globalData;
			
				if (!gd.chatSocketOpen || gd.CHAT == null || gd.wsStatus !== "open") {
					me.stopHeartBeat();
					return;
				}
			
				var userInfo = me.getUserInfoSession();
			
				if (userInfo == null) {
					return;
				}
			
				var dataContent = {
					chatMsg: {
						senderId: userInfo.id,
						msgType: 5
					}
				};
			
				gd.CHAT.send({
					data: JSON.stringify(dataContent),
					fail() {
						console.log("心跳发送失败，准备重连");
			
						gd.chatSocketOpen = false;
						gd.wsStatus = "closed";
			
						me.stopHeartBeat();
						me.scheduleReconnect(0);
					}
				});
			},
			
			// 处理收到的消息
			dealReceiveLastestMsg(msgJSON) {
				console.log(msgJSON);
				var chatMsg = msgJSON.chatMsg;
				var chatTime = msgJSON.chatTime;
				var senderId = chatMsg.senderId;
				
				var receiverType = chatMsg.receiverType;
				console.log('chatMsg.receiverType = ' + receiverType);
				// if (receiverType != 2) {
				// 	return;
				// }
				
				var me = this;
				var userId = me.getUserInfoSession().id;
				var userToken = me.getUserSessionToken();
				var userTokenKey = me.getUserSessionTokenKey();
				var serverUrl = me.globalData.serverUrl;
				uni.request({
					method: "POST",
					header: {
						headerUserTokenKey: userTokenKey,
						headerUserToken: userToken
					},
					url: serverUrl + "/userInfo/get?userId=" + senderId,
					success(result) {
						// console.log(result);
						if (result.data.status == 200) {
							var currentSourceUserInfo = result.data.data;
							me.currentSourceUserInfo = currentSourceUserInfo;
							var msgShow = chatMsg.msg;
							if (chatMsg.msgType == 2) {
								msgShow = "[图片]"
							} else if (chatMsg.msgType == 4) {
								msgShow = "[视频]"
							} else if (chatMsg.msgType == 3) {
								msgShow = "[语音]"
							} 
							me.saveLastestMsgToLocal(senderId, currentSourceUserInfo, msgShow, chatTime, msgJSON);
						}
					}
				})
			},
			
			// 记录每个人的最后一条消息，记录在本地，用于显示聊天列表
			saveLastestMsgToLocal(sourceUserId, sourceUser, msgContent, chatTime, msgJSON) {
				
				var lastMsg = {
					sourceUserId: sourceUserId,		// 源头用户，聊天对象
					name: sourceUser.nickname,
					face: sourceUser.face,
					msgContent: msgContent,
					chatTime: chatTime,
					unReadCounts: 0,
					communicationType: 1, 	// 1:单聊，2:群聊
				}
				console.log(lastMsg);
				// return;
				// 先获得本地存储与候选人聊天的list，如果没有则新创建
				var lastestUserChatList = uni.getStorageSync("lastestUserChatList");
				if (lastestUserChatList == null || lastestUserChatList == undefined || lastestUserChatList == "") {
					lastestUserChatList = [];
				}
				
				// 循环判断，如果存在，则剔除，放入最新的
				var dealMsg = false;
				for ( var i = 0 ; i < lastestUserChatList.length ; i ++) {
					var tmp = lastestUserChatList[i];
					if (tmp.sourceUserId == lastMsg.sourceUserId) {
						lastestUserChatList.splice(i, 1, lastMsg);
						dealMsg = true;
						break;
					}
				}
				if (!dealMsg) {
					lastestUserChatList.unshift(lastMsg);
				}
				uni.setStorageSync("lastestUserChatList", lastestUserChatList);

				// 显示聊天列表
				uni.$emit('reRenderReceiveMsgInMsgVue', "domeafavor");
				// uni.$emit('receiveMsgInMsgVue', res.data);	// 用户处理自己的消息，目前用不到，目的是同步给消息的发送方的多设备里
				
				uni.$emit('receiveMsgInMsgListVue', msgJSON);
			},
			
			sendSocketMessage(msg) {
				var gd = this.globalData;
			
				if (gd.chatSocketOpen && gd.CHAT != null && gd.wsStatus === "open") {
					gd.CHAT.send({
						data: msg,
						fail: () => {
							console.log("消息发送失败，准备重连");
			
							gd.chatSocketOpen = false;
							gd.wsStatus = "closed";
			
							this.stopHeartBeat();
							this.scheduleReconnect(0);
			
							uni.showToast({
								icon: "none",
								title: "消息发送失败，正在重连"
							});
						}
					});
			
					return true;
				}
			
				this.scheduleReconnect(0);
			
				uni.showToast({
					icon: "none",
					title: "聊天连接已断开，正在重连"
				});
			
				return false;
			},
			
			getAppEnv() {
				return this.globalData.env;
			},
			
			removeTabBarBadge(index, number) {
				uni.removeTabBarBadge({
				  index: index,
				})	
			},
			setTabBarRedNumber(index, number) {
				uni.setTabBarBadge({
				  index: index,
				  text: number+""
				})	
			},
			showTabBarRedDot(index) {
				uni.showTabBarRedDot({
					index: index
				});
			},
			hideTabBarRedDot(index) {
				uni.hideTabBarRedDot({
					index: index
				});
			},

			goBack(pages) {
				if (pages == null || pages == "" || pages == undefined) {
					pages = 1;
				}
				uni.navigateBack({
					delta: pages
				})
			},

			closeWSConnect() {
				var gd = this.globalData;
			
				gd.manualClose = true;
				gd.chatSocketOpen = false;
				gd.wsStatus = "closed";
			
				this.clearReconnectTimer();
				this.stopHeartBeat();
			
				if (gd.CHAT != null) {
					try {
						gd.CHAT.close({
							code: 1000,
							reason: "manual close"
						});
					} catch (e) {}
			
					gd.CHAT = null;
				}
			},
			
			
			// 根据生日计算年龄
			getAge(birthday){     
				if (birthday == null || birthday == undefined || birthday =='') {
					return 0;
				}
			    var returnAge;  
			    var strBirthdayArr = birthday.split("-");  
			    var birthYear = strBirthdayArr[0];  
			    var birthMonth = strBirthdayArr[1];  
			    var birthDay = strBirthdayArr[2];  
			      
			    var d = new Date();  
			    var nowYear = d.getFullYear();  
			    var nowMonth = d.getMonth() + 1;  
			    var nowDay = d.getDate();  
			      
			    if(nowYear == birthYear){  
			        returnAge = 0;//同年 则为0岁  
			    }  
			    else{  
			        var ageDiff = nowYear - birthYear ; //年之差  
			        if(ageDiff > 0){  
			            if(nowMonth == birthMonth) {  
			                var dayDiff = nowDay - birthDay;//日之差  
			                if(dayDiff < 0)  
			                {  
			                    returnAge = ageDiff - 1;  
			                }  
			                else  
			                {  
			                    returnAge = ageDiff ;  
			                }  
			            }  
			            else  
			            {  
			                var monthDiff = nowMonth - birthMonth;//月之差  
			                if(monthDiff < 0)  
			                {  
			                    returnAge = ageDiff - 1;  
			                }  
			                else  
			                {  
			                    returnAge = ageDiff ;  
			                }  
			            }  
			        }  
			        else  
			        {  
			            returnAge = -1;//返回-1 表示出生日期输入错误 晚于今天  
			        }  
			    }  
			    return returnAge;//返回周岁年龄  
			},
			
			// 判断是否为空
			isStrEmpty (str) {
				if (str == null || str == undefined || str == "") {
					return true;
				} else {
					return false;
				}
				
			},
			// 判断用户是否登录
			userIsLogin() {
				var userToken = this.getUserSessionToken();
				var userTokenKey = this.getUserSessionTokenKey();
				// var userInfo = this.getUserInfoSession();
				// console.log("userInfo=" + userInfo);
				console.log("userToken=" + userToken);
				 // && !this.isStrEmpty(userInfo)
				if (!this.isStrEmpty(userToken)) {
					return true;
				} else {
					return false;
				}
			},
			
			// 用户登录以后的session存取，token和userInfo
			setUserSessionToken(token){
				uni.setStorageSync("userToken", token);
			},
			getUserSessionToken() {
				var token = uni.getStorageSync("userToken");
				if (this.isStrEmpty(token)) {
					return "";
				}
				return token;
			},
			setUserSessionTokenKey(tokenKey){
				uni.setStorageSync("userTokenKey", tokenKey);
			},
			getUserSessionTokenKey() {
				var token = uni.getStorageSync("userTokenKey");
				if (this.isStrEmpty(token)) {
					return "";
				}
				return token;
			},
			setUserInfoSession(user){
				uni.setStorageSync("userInfo", JSON.stringify(user));
			},
			getUserInfoSession() {
				var user = uni.getStorageSync("userInfo");
				if (this.isStrEmpty(user)) {
					return null;
				}
				return JSON.parse(user);
			},
			clearUserInfo() {
				uni.removeStorageSync("userInfo");
				uni.removeStorageSync("userToken");
			},
			clearUserToken() {
				uni.removeStorageSync("userToken");
			},
			
			graceNumber(number) {
				if (number == 0) {
					return "0";
				} else if (number > 999 && number <= 9999) {
					return (number/1000).toFixed(1) + 'k';
				} else if (number > 9999 && number <= 99999) {
					return (number/10000).toFixed(1) + 'w';
				} else if (number > 99999) {
					return "10w+";
				} 
				return number;
			},
			
			getDateBeforeNow(stringTime) {
				// console.log(stringTime);
				stringTime = new Date(stringTime.replace(/-/g,'/'))
				
				var minute = 1000 * 60;
				var hour = minute * 60;
				var day = hour * 24;
				var week = day * 7;
				var month = day * 30;
					
				var time1 = new Date().getTime(); //当前的时间戳
				// console.log(time1);
				// console.log(new Date(stringTime));
				var time2 = Date.parse(new Date(stringTime)); //指定时间的时间戳
				// console.log(time2);
				var time = time1 - time2;
					
				var result = null;
				if(time < 0) {
					// alert("设置的时间不能早于当前时间！");
					result = stringTime;
				}else if(time/month >= 1){
					result = parseInt(time/month) + "月前";
				}else if(time/week >= 1){
					result = parseInt(time/week) + "周前";
				}else if(time/day >= 1){
					result = parseInt(time/day) + "天前";
				}else if(time/hour >= 1){
					result = parseInt(time/hour) + "小时前";
				}else if(time/minute >= 1){
					result = parseInt(time/minute) + "分钟前";
				}else {
					result = "刚刚";
				}
				// console.log(result);
				return result;
			},
			
			dateFormat(fmt, date) {
			    let ret;
			    const opt = {
			        "Y+": date.getFullYear().toString(),        // 年
			        "m+": (date.getMonth() + 1).toString(),     // 月
			        "d+": date.getDate().toString(),            // 日
			        "H+": date.getHours().toString(),           // 时
			        "M+": date.getMinutes().toString(),         // 分
			        "S+": date.getSeconds().toString()          // 秒
			        // 有其他格式化字符需求可以继续添加，必须转化成字符串
			    };
			    for (let k in opt) {
			        ret = new RegExp("(" + k + ")").exec(fmt);
			        if (ret) {
			            fmt = fmt.replace(ret[1], (ret[1].length == 1) ? (opt[k]) : (opt[k].padStart(ret[1].length, "0")))
			        };
			    };
			    return fmt;
			},
			
		}
	}
</script>

<style>
/*每个页面公共css */

/* .uni-badge--error {
	background-color: #f43530;
} */



.line-wrapper {
	display: flex;
	flex-direction: row;
	justify-content: center;
}
.line {
	background-color: #f6f6f6;
	/* background-color: red; */
	height: 1px;
	width: 100%;
	align-self: center;
}

.spliter {
	border-left: #dcdada solid 1px;
	margin: 0 10px;
	height: 16px;

	align-self: center;
}

.status_bar {
	height: var(--status-bar-height);
	width: 100%;
	background-color: #ecedec;
	
	z-index: 99;
	
	position: fixed;
	top: 0px;
}

.fix-navigation-bar {
	position: fixed;
}

.navigation-bar {
	width: 100%;
	height: 44px;
	
	display: flex;
	flex-direction: row;
	justify-content: space-between;
	
	position: fixed;
	/* top: var(--status-bar-height); */
	
	padding-top: var(--status-bar-height);
	
	
	/* padding: 0px 10px; */
	
	background-color: #ecedec;
	z-index: 99;
	
	
	/* #ifdef H5 */
	/* padding-top: 0; */
	/* #endif */
	/* #ifndef H5 */
	/* padding-top: 44px; */
	/* #endif */
	
	/* #ifdef APP-IOS */
	/* padding-top: env(safe-area-inset-top); */
	/* #endif */
}


.nav-left {
	align-self: center;
}

.back-icon {
	width: 22px;
	height: 22px;
	margin-left: 20px;
}

.nav-middle {
	display: flex;
	flex-direction: column;
	justify-content: flex-start;
	align-self: center;
}

.page-title {
	color: #000000;
	align-self: center;
	font-size: 18px;
	font-weight: 400;
}

.page-info {
	color: #6d6d6d;
	align-self: center;
	font-size: 12px;
	font-weight: 400;
}

.nav-right {
	align-self: center;
}

.white-icon {
	width: 22px;
	height: 22px;
	margin-right: 20px;
}

.item-line-notouch {
	background-color: #fefffe;
}

.item-line-touched {
	background-color: #e5e5e5;
}


/* uni-radio .uni-radio-input.uni-radio-input-checked{
	background-color: #31B9B3!important;
	border-color: #31B9B3!important;
	background-clip: content-box!important;
	padding: 6rpx!important;
	box-sizing: border-box;
} */

/* uni-radio .uni-radio-input.uni-radio-input-checked::before{
	display: none!important;
} */

.uni-radio-input {
	width: 26px;
	height: 26px;
}
uni-radio:not([disabled]) .uni-radio-input:hover {
	border-color: #31B9B3 !important;
}
uni-radio .uni-radio-input.uni-radio-input-checked {
	border-color: #31B9B3 !important;
	background: #31B9B3 !important;
}
uni-radio .uni-radio-input.uni-radio-input-checked::before {
	background: #31B9B3 !important;
	border-color: #31B9B3 !important;
}

.radio-group {
	display: flex;
	flex-direction: column;
	justify-content: flex-start;
}

.radio-item {
	display: flex;
	flex-direction: row;
	justify-content: flex-start;
}

.radio-words {
	align-self: center;
	/* margin-left: 6px; */
	font-size: 17px;
	font-weight: 400;
}

uni-slider .uni-slider-handle-wrapper{
	height: 3px;
}

</style>
