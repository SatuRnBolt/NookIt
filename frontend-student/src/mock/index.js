import http from '../api/request'

// ── in-memory mutable state (for POST/cancel/appeal/create) ──
const state = {
  reservations: [],
  violations: [
    {
      id: 1,
      occurred_at: '2026-05-14',
      roomName: '图书馆A201',
      seatCode: 'A-12',
      violation_type: 'no_checkin',
      description_text: '预约后未在规定时间内签到，系统自动记录违约一次',
      status: 'active',
    },
    {
      id: 2,
      occurred_at: '2026-04-28',
      roomName: '教学楼C103',
      seatCode: 'C-05',
      violation_type: 'late_cancel',
      description_text: '距预约开始不足30分钟取消预约',
      status: 'active',
    },
    {
      id: 3,
      occurred_at: '2026-04-10',
      roomName: '图书馆B302',
      seatCode: 'B-18',
      violation_type: 'no_checkin',
      description_text: '预约后未签到',
      status: 'revoked',
    },
  ],
  feedbacks: [],
  nextReservationId: 100,
  nextFeedbackId: 200,
}

// ── static mock data ──

const rooms = [
  {
    id: 1,
    roomCode: 'LIB-A201',
    roomName: '图书馆A201',
    displayName: '图书馆A201 自习室',
    campus: '邯郸校区',
    building: '图书馆A楼',
    floor: '2F',
    location: '图书馆A楼 2层',
    location_detail: '邯郸校区 图书馆A楼 2层 201室',
    totalCapacity: 48,
    availableSeats: 21,
    openTime: '07:00-22:30',
    roomType: 'standard',
    visibilityScope: 'public',
    room_status: 'active',
    status: 'active',
    openRuleType: 'fixed',
    descriptionText: '安静自习室，配备空调和 Wi-Fi',
  },
  {
    id: 2,
    roomCode: 'LIB-B302',
    roomName: '图书馆B302',
    displayName: '图书馆B302 研修室',
    campus: '邯郸校区',
    building: '图书馆B楼',
    floor: '3F',
    location: '图书馆B楼 3层',
    location_detail: '邯郸校区 图书馆B楼 3层 302室',
    totalCapacity: 24,
    availableSeats: 8,
    openTime: '08:00-21:30',
    roomType: 'premium',
    visibilityScope: 'public',
    room_status: 'active',
    status: 'active',
    openRuleType: 'fixed',
    descriptionText: '高端研修室，配备独立台灯和电源插座',
  },
  {
    id: 3,
    roomCode: 'TCH-C103',
    roomName: '教学楼C103',
    displayName: '教学楼C103 自习教室',
    campus: '枫林校区',
    building: '教学楼C',
    floor: '1F',
    location: '教学楼C 1层',
    location_detail: '枫林校区 教学楼C 1层 103室',
    totalCapacity: 60,
    availableSeats: 35,
    openTime: '06:30-23:00',
    roomType: 'standard',
    visibilityScope: 'public',
    room_status: 'active',
    status: 'active',
    openRuleType: 'flexible',
    descriptionText: '大型自习教室，24小时开放区域',
  },
  {
    id: 4,
    roomCode: 'LIB-A305',
    roomName: '图书馆A305',
    displayName: '图书馆A305 电子阅览室',
    campus: '邯郸校区',
    building: '图书馆A楼',
    floor: '3F',
    location: '图书馆A楼 3层',
    location_detail: '邯郸校区 图书馆A楼 3层 305室',
    totalCapacity: 36,
    availableSeats: 0,
    openTime: '08:00-21:00',
    roomType: 'digital',
    visibilityScope: 'organization',
    room_status: 'inactive',
    status: 'inactive',
    openRuleType: 'fixed',
    descriptionText: '配备电脑的电子阅览室（维护中）',
  },
  {
    id: 5,
    roomCode: 'TCH-D201',
    roomName: '教学楼D201',
    displayName: '教学楼D201 自习室',
    campus: '江湾校区',
    building: '教学楼D',
    floor: '2F',
    location: '教学楼D 2层',
    location_detail: '江湾校区 教学楼D 2层 201室',
    totalCapacity: 40,
    availableSeats: 18,
    openTime: '07:30-22:00',
    roomType: 'standard',
    visibilityScope: 'public',
    room_status: 'active',
    status: 'active',
    openRuleType: 'fixed',
    descriptionText: '标准自习室，靠窗座位视野开阔',
  },
  {
    id: 6,
    roomCode: 'LIB-B410',
    roomName: '图书馆B410',
    displayName: '图书馆B410 研讨室',
    campus: '邯郸校区',
    building: '图书馆B楼',
    floor: '4F',
    location: '图书馆B楼 4层',
    location_detail: '邯郸校区 图书馆B楼 4层 410室',
    totalCapacity: 12,
    availableSeats: 6,
    openTime: '08:30-20:30',
    roomType: 'meeting',
    visibilityScope: 'custom',
    room_status: 'active',
    status: 'active',
    openRuleType: 'fixed',
    descriptionText: '小组研讨室，需至少2人同时预约',
  },
]

function genSeats(roomId, count) {
  const seats = []
  const cols = 8
  for (let i = 0; i < count; i++) {
    const row = Math.floor(i / cols)
    const col = i % cols
    const statuses = ['active', 'active', 'active', 'active', 'occupied', 'occupied', 'maintenance']
    const seatStatus = statuses[i % statuses.length]
    seats.push({
      id: roomId * 1000 + i + 1,
      seatCode: `${String.fromCharCode(65 + row)}${String(col + 1).padStart(2, '0')}`,
      display_label: `${String.fromCharCode(65 + row)}${col + 1}`,
      seatType: i % 7 === 0 ? 'vip' : 'standard',
      rowNo: row,
      colNo: col,
      has_power: i % 3 === 0,
      hasPower: i % 3 === 0,
      is_window_side: col === 0 || col === cols - 1,
      nearWindow: col === 0 || col === cols - 1,
      is_accessible: i % 10 === 0,
      accessible: i % 10 === 0,
      seat_status: seatStatus,
      seatStatus: seatStatus,
      status: seatStatus,
      is_bookable: seatStatus === 'active',
      isBookable: seatStatus === 'active',
      occupied: seatStatus === 'occupied',
      map_x: 40 + (col % cols) * 140,
      map_y: 40 + row * 100,
      map_width: 120,
      map_height: 70,
      mapX: 40 + (col % cols) * 140,
      mapY: 40 + row * 100,
      mapWidth: 120,
      mapHeight: 70,
      mapRotation: 0,
    })
  }
  return seats
}

const seatMaps = {}
const seatMapCache = {}

function getSeatMapForRoom(roomId) {
  if (!seatMaps[roomId]) {
    const room = rooms.find(r => r.id === Number(roomId))
    const cap = room ? room.totalCapacity : 48
    seatMaps[roomId] = {
      id: roomId * 10,
      versionNo: 1,
      mapStatus: 'published',
      map_width: 1200,
      map_height: 800,
      mapWidth: 1200,
      mapHeight: 800,
      backgroundUrl: '',
      publishedAt: '2026-03-01T08:00:00',
      seats: genSeats(roomId, cap),
    }
  }
  return seatMaps[roomId]
}

const notices = [
  {
    id: 1,
    title: '关于期末考试周延长开放时间的通知',
    content:
      '各位同学：\n\n为配合期末考试复习，自2026年6月1日起至6月30日，各校区自习室开放时间调整为：\n\n- 图书馆自习室：06:00 - 23:30\n- 教学楼自习室：06:00 - 24:00\n\n请同学们合理安排学习时间，注意劳逸结合。\n\n教务处 图书馆\n2026年5月10日',
    notice_type: 'system',
    type: 'system',
    published_at: '2026-05-10 15:30',
    publishedAt: '2026-05-10 15:30',
    created_at: '2026-05-10 15:30',
    author_name: '教务处',
    authorName: '教务处',
  },
  {
    id: 2,
    title: '自习室预约规则更新公告',
    content:
      '各位同学：\n\n为进一步优化座位资源利用率，即日起执行以下规则调整：\n\n1. 每人每日最多预约3个时段，每个时段最长4小时\n2. 预约后15分钟内未签到视为违约\n3. 累计违约3次将封禁账号7天\n4. 临时取消预约需在开始时间前30分钟操作\n\n如有疑问请通过「问题反馈」功能提交。\n\n图书馆管理办公室\n2026年4月20日',
    notice_type: 'rule',
    type: 'rule',
    published_at: '2026-04-20 09:00',
    publishedAt: '2026-04-20 09:00',
    created_at: '2026-04-20 09:00',
    author_name: '图书馆管理办公室',
    authorName: '图书馆管理办公室',
  },
  {
    id: 3,
    title: '"世界读书日"阅读打卡活动',
    content:
      '4月23日是世界读书日，图书馆将举办为期一周的阅读打卡活动！\n\n活动时间：4月22日 - 4月28日\n活动地点：各校区图书馆\n活动内容：\n- 每日到馆打卡可获得积分\n- 累计打卡5天可兑换精美书签\n- 累计打卡7天可参与抽奖\n\n欢迎同学们积极参与！',
    notice_type: 'event',
    type: 'event',
    published_at: '2026-04-18 10:00',
    publishedAt: '2026-04-18 10:00',
    created_at: '2026-04-18 10:00',
    author_name: '图书馆活动部',
    authorName: '图书馆活动部',
  },
  {
    id: 4,
    title: '图书馆A楼3层网络设备维护通知',
    content:
      '各位同学：\n\n图书馆A楼3层将于2026年5月18日（周日）8:00-12:00进行网络设备升级维护，届时该楼层 Wi-Fi 将暂停使用，座位预约系统不受影响。\n\n维护期间建议前往B楼或教学楼自习。\n\n给您带来不便敬请谅解。\n\n信息技术中心\n2026年5月14日',
    notice_type: 'maintenance',
    type: 'maintenance',
    published_at: '2026-05-14 14:00',
    publishedAt: '2026-05-14 14:00',
    created_at: '2026-05-14 14:00',
    author_name: '信息技术中心',
    authorName: '信息技术中心',
  },
  {
    id: 5,
    title: '关于新校区自习室上线的通知',
    content:
      '各位同学：\n\n江湾校区新建教学楼D楼自习室现已正式开放，提供40个自习座位，配备独立电源插座和高速 Wi-Fi。\n\n欢迎同学们通过本系统预约使用。\n\n图书馆管理办公室\n2026年3月15日',
    notice_type: 'system',
    type: 'system',
    published_at: '2026-03-15 08:00',
    publishedAt: '2026-03-15 08:00',
    created_at: '2026-03-15 08:00',
    author_name: '图书馆管理办公室',
    authorName: '图书馆管理办公室',
  },
]

// ── route matching ──

function matchRoute(method, url) {
  // Strip query string for matching
  const path = url.split('?')[0]

  const patterns = [
    // Rooms
    { method: 'GET', pattern: /^\/api\/student\/rooms$/, handler: 'listRooms' },
    { method: 'GET', pattern: /^\/api\/student\/rooms\/(\d+)\/seatmap$/, handler: 'getSeatMap' },
    { method: 'GET', pattern: /^\/api\/student\/rooms\/(\d+)$/, handler: 'getRoomDetail' },
    // Seats
    { method: 'GET', pattern: /^\/api\/student\/seats\/(\d+)\/slots$/, handler: 'getSeatSlots' },
    // Reservations
    { method: 'GET', pattern: /^\/api\/student\/reservations$/, handler: 'listReservations' },
    { method: 'POST', pattern: /^\/api\/student\/reservations$/, handler: 'createReservation' },
    { method: 'POST', pattern: /^\/api\/student\/reservations\/(\d+)\/cancel$/, handler: 'cancelReservation' },
    // Violations
    { method: 'GET', pattern: /^\/api\/student\/violations$/, handler: 'listViolations' },
    { method: 'POST', pattern: /^\/api\/student\/violations\/(\d+)\/appeal$/, handler: 'appealViolation' },
    // Notices
    { method: 'GET', pattern: /^\/api\/student\/notices\/(\d+)$/, handler: 'getNoticeDetail' },
    { method: 'GET', pattern: /^\/api\/student\/notices$/, handler: 'listNotices' },
    // Feedback
    { method: 'GET', pattern: /^\/api\/student\/feedbacks$/, handler: 'listFeedbacks' },
    { method: 'POST', pattern: /^\/api\/student\/feedbacks$/, handler: 'createFeedback' },
  ]

  for (const p of patterns) {
    if (method.toUpperCase() !== p.method) continue
    const m = path.match(p.pattern)
    if (m) return { handler: p.handler, params: m.slice(1) }
  }
  return null
}

// ── handler helpers ──

function getParam(config, key, defaultVal) {
  if (config.params && config.params[key] !== undefined) return config.params[key]
  return defaultVal
}

// ── handlers ──

const handlers = {
  listRooms(config) {
    const page = Number(getParam(config, 'page', 1))
    const pageSize = Number(getParam(config, 'pageSize', 20))
    const search = getParam(config, 'search', '').toLowerCase()
    const campus = getParam(config, 'campus', '')

    let filtered = rooms
    if (search) {
      filtered = filtered.filter(
        r =>
          r.roomName.toLowerCase().includes(search) ||
          r.building.toLowerCase().includes(search) ||
          r.location_detail.toLowerCase().includes(search)
      )
    }
    if (campus) {
      filtered = filtered.filter(r => r.campus === campus)
    }

    const total = filtered.length
    const start = (page - 1) * pageSize
    const records = filtered.slice(start, start + pageSize)

    return { records, total, pageNum: page, pageSize, pages: Math.ceil(total / pageSize) || 1 }
  },

  getRoomDetail(config, [roomId]) {
    const room = rooms.find(r => r.id === Number(roomId))
    return room || null
  },

  getSeatMap(config, [roomId]) {
    return getSeatMapForRoom(roomId)
  },

  getSeatSlots(config, [seatId]) {
    // Return a few random occupied slots
    const occupied = []
    const hash = Number(seatId) * 7
    for (let i = 0; i < 4; i++) {
      const slot = ((hash + i * 3 + (new Date().getDate() % 5)) % 15) + 1
      if (!occupied.includes(slot)) occupied.push(slot)
    }
    return { occupiedSlots: occupied.sort((a, b) => a - b), occupied: occupied.sort((a, b) => a - b) }
  },

  listReservations(config) {
    const page = Number(getParam(config, 'page', 1))
    const pageSize = Number(getParam(config, 'pageSize', 10))
    const status = getParam(config, 'status', '')

    let filtered = state.reservations
    if (status) {
      filtered = filtered.filter(r => r.status === status)
    }

    // If empty, seed some initial reservations
    if (state.reservations.length === 0) {
      state.reservations = [
        {
          id: 101,
          roomName: '图书馆A201',
          room_name: '图书馆A201',
          seatCode: 'A12',
          seat_code: 'A12',
          date: '2026-05-16',
          reservation_date: '2026-05-16',
          startHour: 8,
          start_hour: 8,
          endHour: 10,
          end_hour: 10,
          status: 'pending_checkin',
          reservation_status: 'pending_checkin',
          checkinCode: '3847',
          checkin_code: '3847',
          code: '3847',
        },
        {
          id: 102,
          roomName: '教学楼C103',
          room_name: '教学楼C103',
          seatCode: 'C05',
          seat_code: 'C05',
          date: '2026-05-15',
          reservation_date: '2026-05-15',
          startHour: 14,
          start_hour: 14,
          endHour: 17,
          end_hour: 17,
          status: 'completed',
          reservation_status: 'completed',
          checkinCode: '9215',
          checkin_code: '9215',
          code: '9215',
        },
        {
          id: 103,
          roomName: '图书馆B302',
          room_name: '图书馆B302',
          seatCode: 'B08',
          seat_code: 'B08',
          date: '2026-05-14',
          reservation_date: '2026-05-14',
          startHour: 9,
          start_hour: 9,
          endHour: 12,
          end_hour: 12,
          status: 'cancelled',
          reservation_status: 'cancelled',
          checkinCode: '0000',
          checkin_code: '0000',
          code: '0000',
        },
        {
          id: 104,
          roomName: '图书馆B410',
          room_name: '图书馆B410',
          seatCode: 'D03',
          seat_code: 'D03',
          date: '2026-05-15',
          reservation_date: '2026-05-15',
          startHour: 10,
          start_hour: 10,
          endHour: 13,
          end_hour: 13,
          status: 'checked_in',
          reservation_status: 'checked_in',
          checkinCode: '5562',
          checkin_code: '5562',
          code: '5562',
        },
        {
          id: 105,
          roomName: '教学楼D201',
          room_name: '教学楼D201',
          seatCode: 'F06',
          seat_code: 'F06',
          date: '2026-05-13',
          reservation_date: '2026-05-13',
          startHour: 15,
          start_hour: 15,
          endHour: 19,
          end_hour: 19,
          status: 'violated',
          reservation_status: 'violated',
          checkinCode: '----',
          checkin_code: '----',
          code: '----',
        },
      ]
      filtered = state.reservations
      if (status) {
        filtered = filtered.filter(r => r.status === status)
      }
    }

    const total = filtered.length
    const start = (page - 1) * pageSize
    const records = filtered.slice(start, start + pageSize)

    return { records, total, pageNum: page, pageSize, pages: Math.ceil(total / pageSize) || 1 }
  },

  createReservation(config) {
    const body = JSON.parse(config.data || '{}')
    const newReservation = {
      id: state.nextReservationId++,
      roomName: rooms.find(r => r.id === Number(body.roomId))?.roomName || `自习室#${body.seatId || '?'}`,
      room_name: rooms.find(r => r.id === Number(body.roomId))?.roomName || `自习室#${body.seatId || '?'}`,
      seatCode: `S${body.seatId || '?'}`,
      seat_code: `S${body.seatId || '?'}`,
      date: body.date || new Date().toISOString().slice(0, 10),
      reservation_date: body.date || new Date().toISOString().slice(0, 10),
      startHour: body.startHour || 8,
      start_hour: body.startHour || 8,
      endHour: body.endHour || 11,
      end_hour: body.endHour || 11,
      status: 'pending_checkin',
      reservation_status: 'pending_checkin',
      checkinCode: String(Math.floor(1000 + Math.random() * 9000)),
      checkin_code: String(Math.floor(1000 + Math.random() * 9000)),
      code: String(Math.floor(1000 + Math.random() * 9000)),
    }
    state.reservations.unshift(newReservation)
    return newReservation
  },

  cancelReservation(config, [id]) {
    const res = state.reservations.find(r => r.id === Number(id))
    if (res) {
      res.status = 'cancelled'
      res.reservation_status = 'cancelled'
    }
    return null
  },

  listViolations() {
    const stats = {
      total: state.violations.filter(v => v.status === 'active').length,
      accountStatus: 'active',
    }
    return { records: state.violations, stats, violations: state.violations }
  },

  appealViolation(config, [id]) {
    const violation = state.violations.find(v => v.id === Number(id))
    const body = JSON.parse(config.data || '{}')
    if (violation && body.reason) {
      violation.status = 'pending_appeal'
      violation.appeal_reason = body.reason
    }
    return null
  },

  listNotices(config) {
    const page = Number(getParam(config, 'page', 1))
    const pageSize = Number(getParam(config, 'pageSize', 20))
    const type = getParam(config, 'type', '')

    let filtered = notices
    if (type) {
      filtered = filtered.filter(n => n.notice_type === type || n.type === type)
    }

    const total = filtered.length
    const start = (page - 1) * pageSize
    const records = filtered.slice(start, start + pageSize)

    return { records, total, pageNum: page, pageSize, pages: Math.ceil(total / pageSize) || 1 }
  },

  getNoticeDetail(config, [id]) {
    return notices.find(n => n.id === Number(id)) || null
  },

  listFeedbacks(config) {
    const page = Number(getParam(config, 'page', 1))
    const pageSize = Number(getParam(config, 'pageSize', 20))

    if (state.feedbacks.length === 0) {
      state.feedbacks = [
        {
          id: 201,
          title: '图书馆A楼Wi-Fi信号不稳定',
          feedback_type: 'bug',
          type: 'bug',
          feedback_status: 'resolved',
          status: 'resolved',
          content: '最近一周在图书馆A楼2层自习时Wi-Fi频繁掉线，影响学习效率。',
          reply_content: '已联系信息技术中心进行排查，更换了A楼2层的2个AP设备，信号已恢复正常。感谢您的反馈！',
          replyContent: '已联系信息技术中心进行排查，更换了A楼2层的2个AP设备，信号已恢复正常。感谢您的反馈！',
          created_at: '2026-05-08 10:30',
          createdAt: '2026-05-08 10:30',
          replied_at: '2026-05-09 14:20',
          repliedAt: '2026-05-09 14:20',
        },
        {
          id: 202,
          title: '建议增加靠窗座位数量',
          feedback_type: 'suggestion',
          type: 'suggestion',
          feedback_status: 'processing',
          status: 'processing',
          content: '教学楼C103靠窗位置太少，每次都被抢光，能否在窗户一侧增加一些座位？',
          reply_content: '',
          replyContent: '',
          created_at: '2026-05-12 16:45',
          createdAt: '2026-05-12 16:45',
          replied_at: null,
          repliedAt: null,
        },
        {
          id: 203,
          title: '教学楼D栋空调温度过低',
          feedback_type: 'complaint',
          type: 'complaint',
          feedback_status: 'pending',
          status: 'pending',
          content: '教学楼D栋201自习室空调温度设置过低（约20°C），长时间自习体感不适，建议调至24-26°C。',
          reply_content: '',
          replyContent: '',
          created_at: '2026-05-15 09:15',
          createdAt: '2026-05-15 09:15',
          replied_at: null,
          repliedAt: null,
        },
      ]
    }

    const total = state.feedbacks.length
    const start = (page - 1) * pageSize
    const records = [...state.feedbacks].reverse().slice(start, start + pageSize)

    return { records, total, pageNum: page, pageSize, pages: Math.ceil(total / pageSize) || 1 }
  },

  createFeedback(config) {
    const body = JSON.parse(config.data || '{}')
    const fb = {
      id: state.nextFeedbackId++,
      title: body.title || '',
      feedback_type: body.type || 'bug',
      type: body.type || 'bug',
      feedback_status: 'pending',
      status: 'pending',
      content: body.content || '',
      reply_content: '',
      replyContent: '',
      created_at: new Date().toISOString().replace('T', ' ').slice(0, 19),
      createdAt: new Date().toISOString().replace('T', ' ').slice(0, 19),
      replied_at: null,
      repliedAt: null,
    }
    state.feedbacks.push(fb)
    return fb
  },
}

// ── delay helper ──

function delay(ms) {
  return new Promise(resolve => setTimeout(resolve, ms))
}

// ── install mock adapter ──

const originalAdapter = http.defaults.adapter

http.defaults.adapter = async config => {
  const match = matchRoute(config.method, config.url)
  if (match) {
    await delay(200 + Math.random() * 300) // simulate network latency
    try {
      const data = handlers[match.handler](config, match.params)
      return {
        data: { code: 0, message: 'success', data },
        status: 200,
        statusText: 'OK',
        headers: { 'content-type': 'application/json' },
        config,
        request: {},
      }
    } catch (err) {
      return {
        data: { code: 500, message: err.message || 'Mock error' },
        status: 500,
        statusText: 'Internal Server Error',
        headers: {},
        config,
        request: {},
      }
    }
  }
  // pass through to real backend (auth, etc.)
  return originalAdapter(config)
}

console.log('[mock] Student API mock enabled — all /api/student/* requests are intercepted')
