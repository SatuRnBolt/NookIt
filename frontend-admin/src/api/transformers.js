/**
 * 将后端 RoomVO (camelCase) 转换为前端 Rooms.vue 期望的 snake_case 格式
 */
export function roomToFrontend(vo) {
  if (!vo) return vo
  return {
    id: vo.id,
    room_code: vo.roomCode ?? vo.room_code ?? '',
    room_name: vo.roomName ?? vo.room_name ?? '',
    display_name: vo.displayName ?? vo.display_name ?? '',
    campus: vo.campus ?? '',
    building: vo.building ?? '',
    floor: vo.floor ?? '',
    owner_organization: vo.ownerOrganization ?? vo.owner_organization ?? '',
    room_type: vo.roomType ?? vo.room_type ?? '',
    total_capacity: vo.totalCapacity ?? vo.total_capacity ?? 0,
    visibility_scope: vo.visibilityScope ?? vo.visibility_scope ?? 'public',
    room_status: vo.roomStatus ?? vo.room_status ?? 'active',
    open_rule_type: vo.openRuleType ?? vo.open_rule_type ?? 'weekly_schedule',
    location_detail: vo.locationDetail ?? vo.location_detail ?? '',
    description_text: vo.descriptionText ?? vo.description_text ?? '',
    created_at: vo.createdAt ?? vo.created_at ?? '',
    // Keep original IDs for form submission
    campusId: vo.campusId,
    buildingId: vo.buildingId,
    floorId: vo.floorId,
    ownerOrganizationId: vo.ownerOrganizationId,
    roomTypeId: vo.roomTypeId,
  }
}

/**
 * 将前端表单数据 (snake_case) 转换为后端 RoomCreateReq (camelCase)
 */
export function roomFormToBackend(form) {
  return {
    roomCode: form.room_code ?? form.roomCode,
    roomName: form.room_name ?? form.roomName,
    displayName: form.display_name ?? form.displayName,
    campus: form.campus,
    building: form.building,
    floor: form.floor,
    ownerOrganization: form.owner_organization ?? form.ownerOrganization,
    roomType: form.room_type ?? form.roomType,
    totalCapacity: form.total_capacity ?? form.totalCapacity,
    visibilityScope: form.visibility_scope ?? form.visibilityScope,
    openRuleType: form.open_rule_type ?? form.openRuleType,
    roomStatus: form.room_status ?? form.roomStatus,
    locationDetail: form.location_detail ?? form.locationDetail,
    descriptionText: form.description_text ?? form.descriptionText,
    campusId: form.campusId,
    buildingId: form.buildingId,
    floorId: form.floorId,
    ownerOrganizationId: form.ownerOrganizationId,
    roomTypeId: form.roomTypeId,
  }
}

/**
 * 将后端 SeatVO (camelCase) 转换为前端座位地图编辑器期望的 snake_case 格式
 */
export function seatMapSeatToFrontend(vo) {
  if (!vo) return vo
  return {
    id: vo.id,
    seat_code: vo.seatCode ?? vo.seat_code ?? '',
    display_label: vo.displayLabel ?? vo.display_label ?? '',
    seat_type: vo.seatType ?? vo.seat_type ?? 'standard',
    row_no: vo.rowNo ?? vo.row_no ?? 0,
    col_no: vo.colNo ?? vo.col_no ?? 0,
    has_power: vo.hasPower ?? vo.has_power ?? false,
    is_window_side: vo.isWindowSide ?? vo.is_window_side ?? false,
    is_accessible: vo.isAccessible ?? vo.is_accessible ?? false,
    seat_status: vo.seatStatus ?? vo.seat_status ?? 'active',
    is_bookable: vo.isBookable ?? vo.is_bookable ?? true,
    map_x: vo.mapX ?? vo.map_x ?? 0,
    map_y: vo.mapY ?? vo.map_y ?? 0,
    map_width: vo.mapWidth ?? vo.map_width ?? 40,
    map_height: vo.mapHeight ?? vo.map_height ?? 40,
    map_rotation: vo.mapRotation ?? vo.map_rotation ?? 0,
  }
}

/**
 * 将前端座位表单 (snake_case) 转换为后端 SeatInMapReq (camelCase)
 */
export function seatMapSeatToBackend(form) {
  return {
    seatCode: form.seat_code ?? form.seatCode,
    displayLabel: form.display_label ?? form.displayLabel,
    seatType: form.seat_type ?? form.seatType,
    rowNo: form.row_no ?? form.rowNo,
    colNo: form.col_no ?? form.colNo,
    hasPower: form.has_power ?? form.hasPower,
    isWindowSide: form.is_window_side ?? form.isWindowSide,
    isAccessible: form.is_accessible ?? form.isAccessible,
    seatStatus: form.seat_status ?? form.seatStatus,
    isBookable: form.is_bookable ?? form.isBookable,
    mapX: form.map_x ?? form.mapX,
    mapY: form.map_y ?? form.mapY,
    mapWidth: form.map_width ?? form.mapWidth,
    mapHeight: form.map_height ?? form.mapHeight,
    mapRotation: form.map_rotation ?? form.mapRotation,
  }
}

/**
 * 将后端 SeatMapVO (camelCase) 转换为前端期望的 snake_case 格式
 */
export function seatMapToFrontend(vo) {
  if (!vo) return vo
  return {
    id: vo.id,
    study_room_id: vo.studyRoomId ?? vo.study_room_id,
    version_no: vo.versionNo ?? vo.version_no ?? 1,
    map_status: vo.mapStatus ?? vo.map_status ?? 'draft',
    map_width: vo.mapWidth ?? vo.map_width ?? 800,
    map_height: vo.mapHeight ?? vo.map_height ?? 600,
    background_url: vo.backgroundUrl ?? vo.background_url ?? '',
    published_at: vo.publishedAt ?? vo.published_at ?? null,
    created_by: vo.createdBy ?? vo.created_by,
    seats: (vo.seats || []).map(seatMapSeatToFrontend),
  }
}

/**
 * 将后端 SeatAdminVO 转换为前端 Seats.vue 期望格式
 */
export function seatAdminToFrontend(vo) {
  if (!vo) return vo
  return {
    id: vo.id,
    roomId: vo.roomId,
    roomName: vo.roomName ?? '',
    seatNo: vo.seatNo ?? '',
    hasPower: vo.hasPower ?? false,
    nearWindow: vo.nearWindow ?? false,
    status: vo.status ?? 'available',
    createdAt: vo.createdAt ?? '',
  }
}

/**
 * 简化自习室列表 — 用于下拉选择
 */
export function roomSimpleToFrontend(vo) {
  return {
    id: vo.id ?? vo.value,
    name: vo.name ?? vo.roomName ?? vo.label ?? '',
  }
}
