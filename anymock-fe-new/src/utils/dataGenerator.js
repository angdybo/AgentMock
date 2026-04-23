/**
 * 数据生成器工具
 * 用于快速生成各种类型的 Mock 数据
 */

const GENERATORS = {
  // ========== 字符串类 ==========
  randomString: {
    name: '随机字符串',
    desc: '生成指定长度的随机字符串',
    icon: '📝',
    template: (params) => `{{randomString(${params.length || 10})}}`,
    params: [
      { key: { length: 10, name: 'length', desc: '字符串长度', default: 10, min: 1, max: 100 }
    }]
  },

  uuid: {
    name: 'UUID',
    desc: '生成唯一标识符',
    icon: '🔑',
    template: () => '{{uuid()}}',
    params: []
  },

  email: {
    name: '邮箱',
    desc: '生成随机邮箱地址',
    icon: '📧',
    template: () => '{{email()}}',
    params: []
  },

  phone: {
    name: '手机号',
    desc: '生成中国大陆手机号',
    icon: '📱',
    template: () => '{{phone()}}',
    params: []
  },

  idCard: {
    name: '身份证号',
    desc: '生成18位身份证号',
    icon: '🪪',
    template: () => '{{idCard()}}',
    params: []
  },

  chineseName: {
    name: '中文姓名',
    desc: '生成随机中文姓名',
    icon: '👤',
    template: () => '{{chineseName()}}',
    params: []
  },

  englishName: {
    name: '英文姓名',
    desc: '生成随机英文姓名',
    icon: '👤',
    template: () => '{{englishName()}}',
    params: []
  },

  // ========== 数字类 ==========
  randomInt: {
    name: '随机整数',
    desc: '生成指定范围内的随机整数',
    icon: '🔢',
    template: (params) => `{{randomInt(${params.min || 1}, ${params.max || 100})}}`,
    params: [
      { key: 'min', name: '最小值', default: 1, min: -999999, max: 999999 },
      { key: 'max', name: '最大值', default: 100, min: -999999, max: 999999 }
    ]
  },

  randomFloat: {
    name: '随机小数',
    desc: '生成指定范围内的随机小数',
    icon: '🔢',
    template: (params) => `{{randomFloat(${params.min || 0}, ${params.max || 1}, ${params.decimals || 2})}}`,
    params: [
      { key: 'min', name: '最小值', default: 0, min: -999999, max: 999999 },
      { key: 'max', name: '最大值', default: 1, min: -999999, max: 999999 },
      { key: 'decimals', name: '小数位数', default: 2, min: 0, max: 10 }
    ]
  },

  // ========== 日期时间类 ==========
  date: {
    name: '日期',
    desc: '生成指定格式的日期',
    icon: '📅',
    template: (params) => `{{date('${params.format || 'yyyy-MM-dd'}')}}`,
    params: [
      { key: 'format', name: '日期格式', default: 'yyyy-MM-dd' }
    ]
  },

  dateTime: {
    name: '日期时间',
    desc: '生成指定格式的日期时间',
    icon: '🕐',
    template: (params) => `{{dateTime('${params.format || 'yyyy-MM-dd HH:mm:ss'}')}}`,
    params: [
      { key: 'format', name: '日期格式', default: 'yyyy-MM-dd HH:mm:ss' }
    ]
  },

  timestamp: {
    name: '时间戳',
    desc: '生成Unix时间戳（秒）',
    icon: '⏱',
    template: () => '{{timestamp()}}',
    params: []
  },

  now: {
    name: '当前时间',
    desc: '当前日期时间',
    icon: '🕐',
    template: () => '{{now()}}',
    params: []
  },

  // ========== 布尔类 ==========
  randomBoolean: {
    name: '随机布尔值',
    desc: '生成随机的 true/false',
    icon: '✅',
    template: () => '{{randomBoolean()}}',
    params: []
  },

  trueValue: {
    name: '固定 true',
    desc: '始终返回 true',
    icon: '✅',
    template: () => 'true',
    params: []
  },

  falseValue: {
    name: '固定 false',
    desc: '始终返回 false',
    icon: '❌',
    template: () => 'false',
    params: []
  },

  // ========== 数组类 ==========
  randomInts: {
    name: '随机整数数组',
    desc: '生成指定数量的随机整数',
    icon: '📊',
    template: (params) => `{{randomInts(${params.min || 1}, ${params.max || 100}, ${params.count || 5})}}`,
    params: [
      { key: 'min', name: '最小值', default: 1 },
      { key: 'max', name: '最大值', default: 100 },
      { key: 'count', name: '数组长度', default: 5, min: 1, max: 100 }
    ]
  },

  randomStrings: {
    name: '随机字符串数组',
    desc: '生成指定数量的随机字符串',
    icon: '📝',
    template: (params) => `{{randomStrings(${params.length || 10}, ${params.count || 5})}}`,
    params: [
      { key: 'length', name: '字符串长度', default: 10, min: 1, max: 50 },
      { key: 'count', name: '数组长度', default: 5, min: 1, max: 100 }
    ]
  },

  // ========== 对象类 ==========
  address: {
    name: '地址',
    desc: '生成中文地址',
    icon: '🏠',
    template: () => '{{address()}}',
    params: []
  },

  company: {
    name: '公司名称',
    desc: '生成公司名称',
    icon: '🏢',
    template: () => '{{company()}}',
    params: []
  },

  province: {
    name: '省份',
    desc: '生成省份名称',
    icon: '🗺️',
    template: () => '{{province()}}',
    params: []
  },

  city: {
    name: '城市',
    desc: '生成城市名称',
    icon: '🏙️',
    template: () => '{{city()}}',
    params: []
  },

  // ========== 互联网类 ==========
  url: {
    name: 'URL',
    desc: '生成随机URL',
    icon: '🌐',
    template: () => '{{url()}}',
    params: []
  },

  ipv4: {
    name: 'IPv4地址',
    desc: '生成IPv4地址',
    icon: '🌐',
    template: () => '{{ipv4()}}',
    params: []
  },

  macAddress: {
    name: 'MAC地址',
    desc: '生成MAC地址',
    icon: '💻',
    template: () => '{{macAddress()}}',
    params: []
  },

  color: {
    name: '颜色（Hex）',
    desc: '生成16进制颜色',
    icon: '🎨',
    template: () => '{{color()}}',
    params: []
  },

  rgbColor: {
    name: '颜色（RGB）',
    desc: '生成RGB颜色',
    icon: '🎨',
    template: () => '{{rgb()}}',
    params: []
  },

  // ========== 图片类 ==========
  image: {
    name: '图片URL',
    desc: '生成随机图片URL',
    icon: '🖼️',
    template: (params) => `{{image(${params.width || 300}, ${params.height || 200})}}`,
    params: [
      { key: 'width', name: '宽度', default: 300, min: 10, max: 2000 },
      { key: 'height', name: '高度', default: 200, min: 10, max: 2000 }
    ]
  },

  avatar: {
    name: '头像URL',
    desc: '生成头像图片URL',
    icon: '👤',
    template: () => '{{avatar()}}',
    params: []
  },

  // ========== 特殊类 ==========
  nullValue: {
    name: 'null',
    desc: '返回 null',
    icon: '∅',
    template: () => 'null',
    params: []
  },

  emptyString: {
    name: '空字符串',
    desc: '返回空字符串',
    icon: '∅',
    template: () => '""',
    params: []
  },

  emptyArray: {
    name: '空数组',
    desc: '返回空数组',
    icon: '[]',
    template: () => '[]',
    params: []
  },

  emptyObject: {
    name: '空对象',
    desc: '返回空对象',
    icon: '{}',
    template: () => '{}',
    params: []
  }
}

// 按类别分组
const CATEGORIES = {
  '字符串': ['randomString', 'uuid', 'email', 'phone', 'idCard', 'chineseName', 'englishName'],
  '数字': ['randomInt', 'randomFloat'],
  '日期': ['date', 'dateTime', 'timestamp', 'now'],
  '布尔': ['randomBoolean', 'trueValue', 'falseValue'],
  '数组': ['randomInts', 'randomStrings'],
  '对象': ['address', 'company', 'province', 'city'],
  '网络': ['url', 'ipv4', 'macAddress', 'color', 'rgbColor'],
  '图片': ['image', 'avatar'],
  '特殊': ['nullValue', 'emptyString', 'emptyArray', 'emptyObject']
}

/**
 * 获取所有生成器
 */
export function getAllGenerators() {
  return GENERATORS
}

/**
 * 获取分类后的生成器
 */
export function getGeneratorsByCategory() {
  const result = {}
  for (const [category, keys] of Object.entries(CATEGORIES)) {
    result[category] = keys.map(key => ({
      key,
      ...GENERATORS[key]
    }))
  }
  return result
}

/**
 * 根据key获取生成器
 */
export function getGenerator(key) {
  return GENERATORS[key]
}

/**
 * 生成模板字符串
 */
export function generateTemplate(key, params = {}) {
  const generator = GENERATORS[key]
  if (!generator) return ''

  return generator.template(params)
}

/**
 * 获取默认参数值
 */
export function getDefaultParams(key) {
  const generator = GENERATORS[key]
  if (!generator) return {}

  const defaults = {}
  for (const param of generator.params) {
    defaults[param.key] = param.default
  }
  return defaults
}
