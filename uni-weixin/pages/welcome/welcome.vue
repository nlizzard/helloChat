<template>
	<view class="content" @click="enterNext()">
		
		<view class="up-side" :style="{height: upSideHeight+'px', width: starterImgWidth + 'px'}"></view>
		
		<image class="middle-side" src="../../static/images/start_img.png" mode="aspectFit" :style="{height: starterImgHeight+'px', width: starterImgWidth + 'px'}" ></image>
		
		<view class="down-side" :style="{height: downSideHeight+'px', width: starterImgWidth + 'px'}"></view>
	</view>
</template>

<script>
	var app = getApp();
	let sys = uni.getSystemInfoSync();
	const STARTER_IMG_WIDTH = 1086;
	const STARTER_IMG_HEIGHT = 1448;
	const STARTER_IMG_RATIO = STARTER_IMG_HEIGHT / STARTER_IMG_WIDTH;
	export default {
		data() {
			const starterImgWidth = Math.min(sys.windowWidth, sys.windowHeight / STARTER_IMG_RATIO);
			const starterImgHeight = starterImgWidth * STARTER_IMG_RATIO;
			return {
				userIsLogin: false,
				title: 'Hello Lee',
				
				starterImgWidth: starterImgWidth,
				starterImgHeight: starterImgHeight,
				
				upSideHeight: 0,
				downSideHeight: 0,
				enterTimer: null,
				hasEntered: false,
			}
		},
		// watch() {
			
		// },
		onLoad() {
			var windowHeight = sys.windowHeight;
			var leftHeight = windowHeight - this.starterImgHeight;
			this.upSideHeight = leftHeight / 3 * 1;
			this.downSideHeight = leftHeight / 3 * 2;
			// console.log(this.upSideHeight);
			// console.log(this.downSideHeight);
			
			// uni.setTabBarItem({
			// 	index: 4,
			// 	visible: false
			// });
			// uni.hideTabBar();
			
			// 用于清理用户缓存，测试的，这个后面要注释
			// app.clearUserInfo();
			
			// 判断当前用户是否登录，如果没有登录，则跳转至登录页面，如果已经登录，则继续流程
			var me = this;
			var userIsLogin = app.userIsLogin();
			// userIsLogin = true;
			// app.clearUserInfo();
			this.userIsLogin = userIsLogin;
			this.enterTimer = setTimeout(function () {
				me.enterNext();
			}, 2000);
		},
		onUnload() {
			this.clearEnterTimer();
		},
		onShow() {
			// var me = this;
			// var userIsLogin = app.userIsLogin();
			// this.userIsLogin = userIsLogin;
			// setTimeout(function () {
			// 	if (userIsLogin) {
			// 		me.goto();
			// 	} else {
			// 		me.goLogin();
			// 	}
			// }, 1500);
		},
		methods: {
			clearEnterTimer() {
				if (this.enterTimer != null) {
					clearTimeout(this.enterTimer);
					this.enterTimer = null;
				}
			},
			
			enterNext() {
				if (this.hasEntered) {
					return;
				}
				this.hasEntered = true;
				this.clearEnterTimer();
				
				if (this.userIsLogin) {
					this.goto();
				} else {
					// 如果用户信息存在，则跳转到 loginBefore，否则跳转到登录页面
					var userInfo = app.getUserInfoSession();
					if (userInfo != null && userInfo != undefined && userInfo.mobile != null && userInfo.mobile != undefined && userInfo.mobile != "") {
						this.goLoginBefore();
					} else {
						this.goLogin();
					}
				}
			},
			
			goLoginBefore() {
				uni.redirectTo({
					url: "../loginRegist/loginBefore",
					animationType: "fade-in"
				})
			},
			
			goLogin() {
				uni.redirectTo({
					url: "../loginRegist/loginNewAccount",
					animationType: "fade-in"
				})
			},
			
			goto() {
				var me = this;
				uni.switchTab({
					url: "../msgList/msgList"
				});
			}
		}
	}
</script>

<style>
	@import url("welcome.css");
</style>
