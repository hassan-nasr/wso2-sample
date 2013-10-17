define(
"dojo/nls/th/colors", ({
// local representation of all CSS3 named colors, companion to dojo.colors.  To be used where descriptive information
// is required for each color, such as a palette widget, and not for specifying color programatically.
	//Note: due to the SVG 1.0 spec additions, some of these are alternate spellings for the same color (e.g. gray / grey).
	//TODO: should we be using unique rgb values as keys instead and avoid these duplicates, or rely on the caller to do the reverse mapping?
	aliceblue: "alice blue",
	antiquewhite: "antique white",
	aqua: "ฟ้าน้ำทะเล",
	aquamarine: "aquamarine",
	azure: "น้ำเงินฟ้า",
	beige: "น้ำตาลเบจ",
	bisque: "bisque",
	black: "ดำ",
	blanchedalmond: "blanched almond",
	blue: "น้ำเงิน",
	blueviolet: "น้ำเงินม่วง",
	brown: "น้ำตาล",
	burlywood: "burlywood",
	cadetblue: "cadet blue",
	chartreuse: "chartreuse",
	chocolate: "ช็อกโกแลต",
	coral: "coral",
	cornflowerblue: "cornflower blue",
	cornsilk: "cornsilk",
	crimson: "แดงเลือดหมู",
	cyan: "เขียวแกมน้ำเงิน",
	darkblue: "น้ำเงินเข้ม",
	darkcyan: "เขียวแกมน้ำเงินเข้ม",
	darkgoldenrod: "dark goldenrod",
	darkgray: "เทาเข้ม",
	darkgreen: "เขียวเข้ม",
	darkgrey: "เทาเข้ม", // same as darkgray
	darkkhaki: "dark khaki",
	darkmagenta: "แดงแกมม่วงเข้ม",
	darkolivegreen: "เขียวโอลีฟเข้ม",
	darkorange: "ส้มเข้ม",
	darkorchid: "dark orchid",
	darkred: "แดงเข้ม",
	darksalmon: "dark salmon",
	darkseagreen: "dark sea green",
	darkslateblue: "dark slate blue",
	darkslategray: "dark slate gray",
	darkslategrey: "dark slate gray", // same as darkslategray
	darkturquoise: "dark turquoise",
	darkviolet: "ม่วงเข้ม",
	deeppink: "ชมพูเข้ม",
	deepskyblue: "deep sky blue",
	dimgray: "dim gray",
	dimgrey: "dim gray", // same as dimgray
	dodgerblue: "dodger blue",
	firebrick: "สีอิฐ",
	floralwhite: "floral white",
	forestgreen: "forest green",
	fuchsia: "fuchsia",
	gainsboro: "gainsboro",
	ghostwhite: "ghost white",
	gold: "ทอง",
	goldenrod: "goldenrod",
	gray: "เทา",
	green: "เขียว",
	greenyellow: "เขียวแกมเหลือง",
	grey: "เทา", // same as gray
	honeydew: "honeydew",
	hotpink: "hot pink",
	indianred: "indian red",
	indigo: "indigo",
	ivory: "งาช้าง",
	khaki: "khaki",
	lavender: "ม่วงลาเวนเดอร์",
	lavenderblush: "lavender blush",
	lawngreen: "lawn green",
	lemonchiffon: "lemon chiffon",
	lightblue: "น้ำเงินอ่อน",
	lightcoral: "light coral",
	lightcyan: "เขียวแกมน้ำเงินอ่อน",
	lightgoldenrodyellow: "light goldenrod yellow",
	lightgray: "เทาอ่อน",
	lightgreen: "เขียวอ่อน",
	lightgrey: "เทาอ่อน", // same as lightgray
	lightpink: "ชมพูอ่อน",
	lightsalmon: "light salmon",
	lightseagreen: "light sea green",
	lightskyblue: "ฟ้าอ่อน",
	lightslategray: "light slate gray",
	lightslategrey: "light slate gray", // same as lightslategray
	lightsteelblue: "light steel blue",
	lightyellow: "เหลืองอ่อน",
	lime: "เหลืองมะนาว",
	limegreen: "เขียวมะนาว",
	linen: "linen",
	magenta: "แดงแกมม่วง",
	maroon: "น้ำตาลแดง",
	mediumaquamarine: "medium aquamarine",
	mediumblue: "medium blue",
	mediumorchid: "medium orchid",
	mediumpurple: "medium purple",
	mediumseagreen: "medium sea green",
	mediumslateblue: "medium slate blue",
	mediumspringgreen: "medium spring green",
	mediumturquoise: "medium turquoise",
	mediumvioletred: "medium violet-red",
	midnightblue: "midnight blue",
	mintcream: "mint cream",
	mistyrose: "misty rose",
	moccasin: "ม็อคค่า",
	navajowhite: "navajo white",
	navy: "น้ำเงินเข้ม",
	oldlace: "old lace",
	olive: "โอลีฟ",
	olivedrab: "olive drab",
	orange: "ส้ม",
	orangered: "ส้มแกมแดง",
	orchid: "orchid",
	palegoldenrod: "pale goldenrod",
	palegreen: "pale green",
	paleturquoise: "pale turquoise",
	palevioletred: "pale violet-red",
	papayawhip: "papaya whip",
	peachpuff: "peach puff",
	peru: "peru",
	pink: "ชมพู",
	plum: "plum",
	powderblue: "powder blue",
	purple: "ม่วง",
	red: "แดง",
	rosybrown: "rosy brown",
	royalblue: "royal blue",
	saddlebrown: "saddle brown",
	salmon: "salmon",
	sandybrown: "sandy brown",
	seagreen: "sea green",
	seashell: "seashell",
	sienna: "sienna",
	silver: "เงิน",
	skyblue: "sky blue",
	slateblue: "slate blue",
	slategray: "slate gray",
	slategrey: "slate gray", // same as slategray
	snow: "snow",
	springgreen: "spring green",
	steelblue: "steel blue",
	tan: "tan",
	teal: "teal",
	thistle: "thistle",
	tomato: "tomato",
	transparent: "สีใส",
	turquoise: "turquoise",
	violet: "ม่วง",
	wheat: "wheat",
	white: "ขาว",
	whitesmoke: "ขาวควัน",
	yellow: "เหลือง",
	yellowgreen: "เหลืองแกมเขียว"
})
);
