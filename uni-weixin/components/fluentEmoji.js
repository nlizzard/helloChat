const FLUENT_EMOJI_BASE = "../../static/images/msface/";

const fluentEmojiSource = [
	["001", 0x1F600],
	["002", 0x1F603],
	["003", 0x1F601],
	["004", 0x1F606],
	["005", 0x1F60A],
	["006", 0x1F602],
	["007", 0x1F923],
	["008", 0x1F609],
	["009", 0x1F60D],
	["010", 0x1F618],
	["011", 0x1F914],
	["012", 0x1F92A],
	["013", 0x1F610],
	["014", 0x1F611],
	["015", 0x1F644],
	["016", 0x1F622],
	["017", 0x1F62D],
	["018", 0x1F620],
	["019", 0x1F621],
	["020", 0x1F631],
	["021", 0x1F633],
	["022", 0x1F60E],
	["023", 0x1F973],
	["024", 0x1F634],
	["025", 0x1F60B],
	["026", 0x1F607],
	["027", 0x1F970],
	["028", 0x1F617],
	["029", 0x1F619],
	["030", 0x1F61A],
	["031", 0x1F642],
	["032", 0x1F643],
	["033", 0x1FAE0],
	["034", 0x1F92D],
	["035", 0x1F917],
	["036", 0x1F928],
	["037", 0x1F9D0],
	["038", 0x1F913],
	["039", 0x1F615],
	["040", 0x1F61F],
	["041", 0x1F641],
	["042", 0x1F62E],
	["043", 0x1F62F],
	["044", 0x1F632],
	["045", 0x1F92F],
	["046", 0x1F628],
	["047", 0x1F630],
	["048", 0x1F625],
	["049", 0x1F613],
	["050", 0x1F616],
	["051", 0x1F623],
	["052", 0x1F61E],
	["053", 0x1F60C],
	["054", 0x1F629],
	["055", 0x1F62B],
	["056", 0x1F971],
	["057", 0x1F624],
	["058", 0x1F92C],
	["059", 0x1F608],
	["060", 0x1F47F],
	["061", 0x1F480],
	["062", 0x1F4A9],
	["063", 0x1F921],
];

export const fluentEmojiList = fluentEmojiSource.map(function(item) {
	return {
		id: item[0],
		emoji: String.fromCodePoint(item[1]),
		src: FLUENT_EMOJI_BASE + item[0] + ".png",
	};
});

export function getFluentEmojiById(id) {
	for (var i = 0; i < fluentEmojiList.length; i++) {
		if (fluentEmojiList[i].id == id) {
			return fluentEmojiList[i];
		}
	}
	return null;
}

export function getFluentEmojiByEmoji(emoji) {
	for (var i = 0; i < fluentEmojiList.length; i++) {
		if (fluentEmojiList[i].emoji == emoji) {
			return fluentEmojiList[i];
		}
	}
	return null;
}

export function isFluentEmojiId(id) {
	return getFluentEmojiById(id) != null;
}

export function replaceFluentEmojiWithImages(msg, imageClass) {
	if (msg == null || msg == undefined) {
		return "";
	}
	msg = msg + "";
	imageClass = imageClass || "emoji-iamge";
	
	for (var i = 0; i < fluentEmojiList.length; i++) {
		var face = fluentEmojiList[i];
		var img = "<img src='" + face.src + "' class='" + imageClass + "'/>";
		msg = msg.split("[" + face.id + "]").join(img);
		msg = msg.split(face.emoji).join(img);
	}
	
	return msg;
}
