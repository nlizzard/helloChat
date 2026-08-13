<!-- 设置拍一拍 -->
<!-- 说明：原页面为 myNickname 改造的半成品，保存按钮无 @click、方法引用不存在的 this.nickname，
     且后端 Users/ModifyUserBO 暂无拍一拍字段，保存逻辑无后端支撑。在补齐后端字段前，先以"功能暂未开放"提示，避免死页面与空指针。 -->
<template>
	<view class="page">

		<view class="title-wrapper">
			<text class="update-title-words">设置拍一拍</text>
			<text class="title-tips"></text>

			<view class="number-box">
				<text class="lable-wechat">该功能暂未开放，敬请期待</text>
			</view>

			<text class="tips">设置后，朋友拍你的时候将会出现。</text>

		</view>

		<view :class="{'button-box-gray': true,'button-box': true}">
			<text class="button-words">确定</text>
		</view>


	</view>
</template>

<script>
	export default {
		data() {
			return {
			}
		},
		onShow() {
		},
		methods: {
		}
	}
</script>

<style>
	@import url("paiyipai.css");
</style>
