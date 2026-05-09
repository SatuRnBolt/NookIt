<template>
  <div class="rooms-page">
    <!-- Page Header -->
    <div class="page-header">
      <div>
        <div class="page-header-title">
          <el-icon class="title-icon"><OfficeBuilding /></el-icon>
          自习室管理
        </div>
        <div class="page-header-sub">管理各校区自习室基本信息、启用状态与座位布局</div>
      </div>
      <el-button type="primary" icon="Plus" @click="openDialog()">新增自习室</el-button>
    </div>

    <!-- Stats Row -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-icon-wrap stat-blue"><el-icon :size="20"><OfficeBuilding /></el-icon></div>
        <div>
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">自习室总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrap stat-green"><el-icon :size="20"><CircleCheck /></el-icon></div>
        <div>
          <div class="stat-value">{{ stats.active }}</div>
          <div class="stat-label">启用中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrap stat-purple"><el-icon :size="20"><Grid /></el-icon></div>
        <div>
          <div class="stat-value">{{ stats.capacity }}</div>
          <div class="stat-label">总座位容量</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon-wrap stat-orange"><el-icon :size="20"><Warning /></el-icon></div>
        <div>
          <div class="stat-value">{{ stats.inactive }}</div>
          <div class="stat-label">已停用</div>
        </div>
      </div>
    </div>

    <!-- Filter Bar -->
    <div class="filter-bar">
      <div class="filter-bar-left">
        <el-input
          v-model="search"
          placeholder="搜索房间编码 / 名称"
          prefix-icon="Search"
          clearable
          style="width: 240px"
        />
        <el-select v-model="campusFilter" placeholder="所属校区" clearable style="width: 140px">
          <el-option v-for="c in campusOptions" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="visibilityFilter" placeholder="可见范围" clearable style="width: 130px">
          <el-option label="全校公开" value="public" />
          <el-option label="机构专属" value="organization" />
          <el-option label="自定义" value="custom" />
        </el-select>
      </div>
      <div class="filter-bar-right">
        <span class="filter-count">共 {{ filteredRooms.length }} 条</span>
      </div>
    </div>

    <div class="content-card">
      <el-table :data="pagedRooms" stripe style="width: 100%" row-key="id">
        <el-table-column prop="room_code" label="房间编码" width="130">
          <template #default="{ row }">
            <span class="code-text">{{ row.room_code }}</span>
          </template>
        </el-table-column>

        <el-table-column label="房间名称" min-width="180">
          <template #default="{ row }">
            <div class="room-name">{{ row.room_name }}</div>
            <div v-if="row.display_name" class="room-alias">{{ row.display_name }}</div>
          </template>
        </el-table-column>

        <el-table-column label="位置" min-width="200">
          <template #default="{ row }">
            <div class="location-line">
              <el-tag type="primary" size="small" effect="plain">{{ row.campus }}</el-tag>
              <span class="location-detail">{{ row.building }} {{ row.floor }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="所属机构" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.owner_organization" type="warning" size="small" effect="plain">
              {{ row.owner_organization }}
            </el-tag>
            <span v-else class="text-muted">全校开放</span>
          </template>
        </el-table-column>

        <el-table-column prop="room_type" label="类型" width="120" />

        <el-table-column prop="total_capacity" label="容量" width="72" align="center">
          <template #default="{ row }">
            <span class="capacity-text">{{ row.total_capacity }}</span>
          </template>
        </el-table-column>

        <el-table-column label="可见范围" width="96" align="center">
          <template #default="{ row }">
            <el-tag :type="visibilityType(row.visibility_scope)" size="small" effect="light">
              {{ visibilityLabel(row.visibility_scope) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="启用状态" width="96" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.room_status"
              active-value="active"
              inactive-value="inactive"
              style="--el-switch-on-color: #22c55e"
              @change="onStatusChange(row)"
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="viewSeatMap(row)">
              <el-icon style="margin-right: 2px"><Grid /></el-icon>
              座位地图
            </el-button>
            <el-divider direction="vertical" />
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-divider direction="vertical" />
            <el-button link type="danger" @click="deleteRoom(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-bar">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="filteredRooms.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
        />
      </div>
    </div>

    <el-dialog
      v-model="seatMapVisible"
      width="1100px"
      destroy-on-close
      class="seat-map-dialog"
    >
      <template #header>
        <div class="seat-dialog-header">
          <div>
            <div class="seat-dialog-title">
              <span class="code-text">{{ currentRoom?.room_code }}</span>
              <span>{{ currentRoom?.room_name }}</span>
            </div>
            <div class="seat-dialog-meta">
              {{ currentRoom?.campus }} / {{ currentRoom?.building }} {{ currentRoom?.floor }}
              <span class="meta-divider">|</span>
              当前版本 V{{ currentSeatMap?.version_no }}
              <span class="meta-divider">|</span>
              布局状态 {{ mapStatusText(currentSeatMap?.map_status) }}
            </div>
          </div>

          <div class="header-actions">
            <el-radio-group v-model="layoutMode" size="small">
              <el-radio-button label="view">查看</el-radio-button>
              <el-radio-button label="edit">编辑布局</el-radio-button>
            </el-radio-group>
            <el-tag :type="mapStatusType(currentSeatMap?.map_status)" effect="light">
              {{ mapStatusText(currentSeatMap?.map_status) }}
            </el-tag>
          </div>
        </div>
      </template>

      <div class="map-toolbar">
        <div class="map-toolbar-left">
          <div class="summary-chip">
            总座位 <strong>{{ currentSeatMapSeats.length }}</strong>
          </div>
          <div class="summary-chip active-chip">
            正常 <strong>{{ seatSummary.active }}</strong>
          </div>
          <div class="summary-chip inactive-chip">
            停用 <strong>{{ seatSummary.inactive }}</strong>
          </div>
          <div class="summary-chip maintenance-chip">
            维护 <strong>{{ seatSummary.maintenance }}</strong>
          </div>
          <div class="summary-chip bookable-chip">
            可预约 <strong>{{ seatSummary.bookable }}</strong>
          </div>
        </div>

        <div class="map-toolbar-right">
          <el-select v-model="seatStatusFilter" placeholder="筛选状态" clearable style="width: 130px">
            <el-option label="正常" value="active" />
            <el-option label="停用" value="inactive" />
            <el-option label="维护中" value="maintenance" />
          </el-select>
          <el-switch v-model="onlyBookable" />
          <span class="filter-label">仅看可预约</span>
          <el-button v-if="layoutMode === 'edit'" plain @click="createNewMapVersion">新建版本</el-button>
          <el-button
            v-if="layoutMode === 'edit'"
            :type="addSeatMode ? 'primary' : 'default'"
            @click="toggleAddSeatMode"
          >
            {{ addSeatMode ? '点击画布放置中' : '新增座位' }}
          </el-button>
          <el-button v-if="layoutMode === 'edit'" plain :disabled="!selectedSeat" @click="duplicateSeat">
            复制座位
          </el-button>
          <el-button
            v-if="layoutMode === 'edit'"
            plain
            type="danger"
            :disabled="!selectedSeat"
            @click="removeSeat"
          >
            删除座位
          </el-button>
          <el-button v-if="layoutMode === 'edit'" type="primary" @click="saveLayoutDraft">保存草稿</el-button>
          <el-button v-if="layoutMode === 'edit'" type="success" @click="publishCurrentMap">发布布局</el-button>
        </div>
      </div>

      <div class="seat-map-layout">
        <section class="map-stage-card">
          <div class="stage-title-row">
            <div>
              <div class="stage-title">教室布局图</div>
              <div class="stage-subtitle">
                {{ layoutMode === 'edit' ? '可新增座位、调整位置、维护版本草稿' : '点击座位查看详情与状态' }}
              </div>
            </div>
            <div v-if="layoutMode === 'edit'" class="edit-hint">
              开启“新增座位”后，点击画布空白处即可落点
            </div>
          </div>

          <div class="room-front-banner">
            <span>讲台 / 前门方向</span>
          </div>

          <div class="map-stage-scroll">
            <div
              v-if="currentSeatMap"
              ref="mapStageRef"
              class="map-stage"
              :class="{ editable: layoutMode === 'edit', placing: addSeatMode }"
              :style="mapStageStyle"
              @click="onMapStageClick"
            >
              <div class="window-band">窗边</div>
              <div class="door-marker">入口</div>

              <button
                v-for="seat in visibleSeats"
                :key="seat.id"
                type="button"
                class="seat-node"
                :class="[
                  `seat-${seat.seat_status}`,
                  { selected: selectedSeat?.id === seat.id, unbookable: !seat.is_bookable }
                ]"
                :style="seatStyle(seat)"
                @click.stop="selectSeat(seat)"
              >
                <span class="seat-node-label">{{ seat.display_label }}</span>
                <span class="seat-node-code">{{ seat.seat_code }}</span>
                <el-icon v-if="seat.has_power" class="seat-power-icon"><Lightning /></el-icon>
              </button>
            </div>
          </div>

          <div class="legend-row">
            <div class="legend-item">
              <span class="legend-dot seat-active"></span>
              正常
            </div>
            <div class="legend-item">
              <span class="legend-dot seat-inactive"></span>
              停用
            </div>
            <div class="legend-item">
              <span class="legend-dot seat-maintenance"></span>
              维护中
            </div>
            <div class="legend-item">
              <span class="legend-lock"></span>
              不可预约
            </div>
          </div>
        </section>

        <aside class="seat-side-panel">
          <div class="panel-title">
            {{ layoutMode === 'edit' ? '布局编辑' : '座位详情' }}
          </div>

          <template v-if="layoutMode === 'edit'">
            <div class="config-card">
              <div class="config-card-title">地图配置</div>
              <div class="seat-grid">
                <el-form-item label="画布宽度">
                  <el-input-number v-model="mapForm.map_width" :min="400" :step="20" controls-position="right" style="width: 100%" />
                </el-form-item>
                <el-form-item label="画布高度">
                  <el-input-number v-model="mapForm.map_height" :min="300" :step="20" controls-position="right" style="width: 100%" />
                </el-form-item>
              </div>
              <el-form-item label="背景图地址">
                <el-input v-model="mapForm.background_url" placeholder="可填写教室平面图 URL" />
              </el-form-item>
              <el-form-item label="地图状态">
                <el-select v-model="mapForm.map_status" style="width: 100%">
                  <el-option label="草稿" value="draft" />
                  <el-option label="已发布" value="published" />
                  <el-option label="已归档" value="archived" />
                </el-select>
              </el-form-item>
              <el-button plain style="width: 100%" @click="applyMapSettings">应用地图设置</el-button>
            </div>
          </template>

          <template v-if="selectedSeat">
            <div class="seat-profile">
              <div>
                <div class="seat-profile-code">{{ selectedSeat.seat_code }}</div>
                <div class="seat-profile-name">{{ selectedSeat.display_label }}</div>
              </div>
              <el-tag :type="seatStatusType(selectedSeat.seat_status)" effect="light">
                {{ seatStatusText(selectedSeat.seat_status) }}
              </el-tag>
            </div>

            <el-form label-position="top" class="seat-form">
              <el-form-item label="座位编码">
                <el-input v-model="seatForm.seat_code" :disabled="layoutMode !== 'edit'" />
              </el-form-item>

              <el-form-item label="显示名称">
                <el-input v-model="seatForm.display_label" :disabled="layoutMode !== 'edit'" />
              </el-form-item>

              <el-form-item label="座位状态">
                <el-radio-group v-model="seatForm.seat_status" :disabled="layoutMode !== 'edit'" size="small">
                  <el-radio-button label="active">正常</el-radio-button>
                  <el-radio-button label="inactive">停用</el-radio-button>
                  <el-radio-button label="maintenance">维护中</el-radio-button>
                </el-radio-group>
              </el-form-item>

              <el-form-item label="座位类型">
                <el-select v-model="seatForm.seat_type" :disabled="layoutMode !== 'edit'" style="width: 100%">
                  <el-option label="普通座" value="standard" />
                  <el-option label="高脚桌" value="high-desk" />
                  <el-option label="单人隔间" value="booth" />
                  <el-option label="研讨座" value="group" />
                </el-select>
              </el-form-item>

              <div class="seat-grid">
                <el-form-item label="行号">
                  <el-input-number v-model="seatForm.row_no" :min="1" :disabled="layoutMode !== 'edit'" controls-position="right" style="width: 100%" />
                </el-form-item>
                <el-form-item label="列号">
                  <el-input-number v-model="seatForm.col_no" :min="1" :disabled="layoutMode !== 'edit'" controls-position="right" style="width: 100%" />
                </el-form-item>
              </div>

              <div class="seat-grid">
                <el-form-item label="X 坐标">
                  <el-input-number v-model="seatForm.map_x" :min="0" :step="5" :disabled="layoutMode !== 'edit'" controls-position="right" style="width: 100%" />
                </el-form-item>
                <el-form-item label="Y 坐标">
                  <el-input-number v-model="seatForm.map_y" :min="0" :step="5" :disabled="layoutMode !== 'edit'" controls-position="right" style="width: 100%" />
                </el-form-item>
              </div>

              <div v-if="layoutMode === 'edit'" class="nudge-row">
                <el-button @click="nudgeSeat(0, -10)">上移</el-button>
                <el-button @click="nudgeSeat(-10, 0)">左移</el-button>
                <el-button @click="nudgeSeat(10, 0)">右移</el-button>
                <el-button @click="nudgeSeat(0, 10)">下移</el-button>
              </div>

              <div class="seat-grid">
                <el-form-item label="宽度">
                  <el-input-number v-model="seatForm.map_width" :min="24" :step="2" :disabled="layoutMode !== 'edit'" controls-position="right" style="width: 100%" />
                </el-form-item>
                <el-form-item label="高度">
                  <el-input-number v-model="seatForm.map_height" :min="24" :step="2" :disabled="layoutMode !== 'edit'" controls-position="right" style="width: 100%" />
                </el-form-item>
              </div>

              <el-form-item label="旋转角度">
                <el-slider v-model="seatForm.map_rotation" :min="-45" :max="45" :step="5" :disabled="layoutMode !== 'edit'" show-input />
              </el-form-item>

              <div class="feature-list">
                <el-checkbox v-model="seatForm.is_bookable" :disabled="layoutMode !== 'edit'">允许预约</el-checkbox>
                <el-checkbox v-model="seatForm.has_power" :disabled="layoutMode !== 'edit'">带电源</el-checkbox>
                <el-checkbox v-model="seatForm.is_window_side" :disabled="layoutMode !== 'edit'">靠窗</el-checkbox>
                <el-checkbox v-model="seatForm.is_accessible" :disabled="layoutMode !== 'edit'">无障碍座位</el-checkbox>
              </div>

              <div v-if="layoutMode === 'edit'" class="quick-actions">
                <el-button @click="setSeatStatus('active')">设为正常</el-button>
                <el-button @click="setSeatStatus('maintenance')">设为维护</el-button>
                <el-button @click="setSeatStatus('inactive')">设为停用</el-button>
              </div>

              <el-button
                type="primary"
                style="width: 100%"
                :disabled="layoutMode !== 'edit'"
                @click="saveSeatConfig"
              >
                保存座位设置
              </el-button>
            </el-form>
          </template>

          <div v-else class="empty-seat-panel">
            <el-icon :size="34"><Position /></el-icon>
            <p>{{ layoutMode === 'edit' ? '先选中一个座位，或者在画布新增一个座位' : '在左侧布局图中点击一个座位' }}</p>
            <span>{{ layoutMode === 'edit' ? '可以继续调整坐标、尺寸、状态和预约属性' : '这里会显示座位状态、位置和预约设置' }}</span>
          </div>
        </aside>
      </div>
    </el-dialog>

    <el-dialog
      v-model="dialogVisible"
      :title="editingRoom ? '编辑自习室' : '新增自习室'"
      width="640px"
      destroy-on-close
      class="room-dialog"
    >
      <el-form :model="form" label-position="top" class="dialog-form">
        <div class="form-section-title">基本信息</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="房间编码" required>
              <el-input v-model="form.room_code" placeholder="如 LIB-A-301" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="房间类型" required>
              <el-select v-model="form.room_type" style="width: 100%">
                <el-option v-for="t in roomTypeOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="14">
            <el-form-item label="房间名称" required>
              <el-input v-model="form.room_name" placeholder="如 图书馆自习室 A" />
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="显示名称">
              <el-input v-model="form.display_name" placeholder="可选简称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider />

        <div class="form-section-title">位置信息</div>
        <el-row :gutter="20">
          <el-col :span="10">
            <el-form-item label="所属校区" required>
              <el-select v-model="form.campus" style="width: 100%">
                <el-option v-for="c in campusOptions" :key="c" :label="c" :value="c" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="9">
            <el-form-item label="所属楼栋" required>
              <el-input v-model="form.building" placeholder="如 图书馆" />
            </el-form-item>
          </el-col>
          <el-col :span="5">
            <el-form-item label="楼层">
              <el-input v-model="form.floor" placeholder="3F" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="位置说明">
          <el-input v-model="form.location_detail" placeholder="如 南门右侧走廊尽头" />
        </el-form-item>

        <el-divider />

        <div class="form-section-title">权限与规则</div>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="所属机构">
              <el-select v-model="form.owner_organization" placeholder="全校开放可不选" clearable style="width: 100%">
                <el-option v-for="o in orgOptions" :key="o" :label="o" :value="o" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="可见范围">
              <el-select v-model="form.visibility_scope" style="width: 100%">
                <el-option label="全校公开" value="public" />
                <el-option label="机构专属" value="organization" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="总容量" required>
              <el-input-number v-model="form.total_capacity" :min="1" :max="1000" style="width: 100%" controls-position="right" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开放规则">
              <el-select v-model="form.open_rule_type" style="width: 100%">
                <el-option label="固定时段" value="fixed" />
                <el-option label="每周排班" value="weekly_schedule" />
                <el-option label="自定义" value="custom" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="启用状态">
              <div class="switch-row">
                <el-switch
                  v-model="form.room_status"
                  active-value="active"
                  inactive-value="inactive"
                  style="--el-switch-on-color: #22c55e"
                />
                <span class="switch-label">{{ form.room_status === 'active' ? '启用中' : '已停用' }}</span>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="描述">
          <el-input v-model="form.description_text" type="textarea" :rows="2" placeholder="可选的详细说明" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRoom">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockRooms } from '../mock/data'

const rooms = ref([...mockRooms.map((room) => ({
  ...room,
  room_status: room.room_status === 'draft' || room.room_status === 'closed' ? 'inactive' : room.room_status,
}))])

const search = ref('')
const campusFilter = ref('')
const visibilityFilter = ref('')
const dialogVisible = ref(false)
const editingRoom = ref(null)

const seatMapVisible = ref(false)
const currentRoom = ref(null)
const currentSeatMap = ref(null)
const selectedSeat = ref(null)
const seatStatusFilter = ref('')
const onlyBookable = ref(false)
const layoutMode = ref('view')
const addSeatMode = ref(false)
const mapStageRef = ref(null)

const campusOptions = ['邯郸校区', '枫林校区', '江湾校区', '张江校区']
const orgOptions = ['计算机学院', '信息工程学院', '数学学院', '物理学院', '化学学院', '医学院', '人文学院', '经济学院']
const roomTypeOptions = ['普通自习室', '大型自习室', '机构专属自习室', '安静自习室', '研讨室']

const form = reactive(defaultForm())
const seatForm = reactive(defaultSeatForm())
const mapForm = reactive(defaultMapForm())
const seatMapsByRoom = reactive(createSeatMapsForRooms(rooms.value))

const currentPage = ref(1)
const pageSize = ref(10)

const filteredRooms = computed(() => rooms.value.filter((room) => {
  if (search.value && !room.room_code.includes(search.value) && !room.room_name.includes(search.value)) return false
  if (campusFilter.value && room.campus !== campusFilter.value) return false
  if (visibilityFilter.value && room.visibility_scope !== visibilityFilter.value) return false
  return true
}))

watch([search, campusFilter, visibilityFilter], () => { currentPage.value = 1 })

const pagedRooms = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRooms.value.slice(start, start + pageSize.value)
})

const stats = computed(() => ({
  total: rooms.value.length,
  active: rooms.value.filter(r => r.room_status === 'active').length,
  capacity: rooms.value.reduce((sum, r) => sum + (r.total_capacity || 0), 0),
  inactive: rooms.value.filter(r => r.room_status !== 'active').length,
}))

const currentSeatMapSeats = computed(() => currentSeatMap.value?.seats ?? [])

const visibleSeats = computed(() => currentSeatMapSeats.value.filter((seat) => {
  if (seatStatusFilter.value && seat.seat_status !== seatStatusFilter.value) return false
  if (onlyBookable.value && !seat.is_bookable) return false
  return true
}))

const seatSummary = computed(() => currentSeatMapSeats.value.reduce((summary, seat) => {
  if (summary[seat.seat_status] !== undefined) summary[seat.seat_status] += 1
  if (seat.is_bookable) summary.bookable += 1
  return summary
}, { active: 0, inactive: 0, maintenance: 0, bookable: 0 }))

const mapStageStyle = computed(() => {
  if (!currentSeatMap.value) return {}
  return {
    width: `${currentSeatMap.value.map_width}px`,
    height: `${currentSeatMap.value.map_height}px`,
    backgroundImage: currentSeatMap.value.background_url ? `url(${currentSeatMap.value.background_url})` : 'none',
  }
})

function defaultForm() {
  return {
    room_code: '',
    room_name: '',
    display_name: '',
    campus: '邯郸校区',
    building: '',
    floor: '',
    owner_organization: null,
    room_type: '普通自习室',
    total_capacity: 30,
    visibility_scope: 'public',
    open_rule_type: 'weekly_schedule',
    room_status: 'active',
    location_detail: '',
    description_text: '',
  }
}

function defaultSeatForm() {
  return {
    seat_code: '',
    display_label: '',
    seat_type: 'standard',
    row_no: 1,
    col_no: 1,
    has_power: false,
    is_window_side: false,
    is_accessible: false,
    seat_status: 'active',
    is_bookable: true,
    map_x: 0,
    map_y: 0,
    map_width: 44,
    map_height: 44,
    map_rotation: 0,
  }
}

function defaultMapForm() {
  return {
    background_url: '',
    map_width: 750,
    map_height: 520,
    map_status: 'draft',
  }
}

function createSeatMapsForRooms(roomList) {
  return Object.fromEntries(roomList.map((room, index) => [room.id, buildMockSeatMap(room, index)]))
}

function buildMockSeatMap(room, roomIndex) {
  const columnsPerBlock = room.total_capacity >= 70 ? 5 : 4
  const totalColumns = columnsPerBlock * 2
  const rows = Math.ceil(room.total_capacity / totalColumns)
  const seatWidth = 44
  const seatHeight = 44
  const gap = 14
  const aisle = 70
  const paddingX = 60
  const paddingY = 90
  const mapWidth = paddingX * 2 + (columnsPerBlock * seatWidth * 2) + (gap * (totalColumns - 2)) + aisle
  const mapHeight = paddingY * 2 + (rows * seatHeight) + (Math.max(rows - 1, 0) * gap) + 40
  const seats = []
  const statusByIndex = ['active', 'active', 'active', 'maintenance', 'active', 'inactive']

  for (let i = 0; i < room.total_capacity; i += 1) {
    const rowIndex = Math.floor(i / totalColumns)
    const colIndex = i % totalColumns
    const isRightBlock = colIndex >= columnsPerBlock
    const innerCol = isRightBlock ? colIndex - columnsPerBlock : colIndex
    const visualCol = innerCol + 1 + (isRightBlock ? columnsPerBlock : 0)
    const baseX = paddingX + innerCol * (seatWidth + gap) + (isRightBlock ? columnsPerBlock * (seatWidth + gap) + aisle : 0)
    const baseY = paddingY + rowIndex * (seatHeight + gap)
    const seatCode = `${String.fromCharCode(65 + rowIndex)}${String(visualCol).padStart(2, '0')}`
    const status = statusByIndex[(i + roomIndex) % statusByIndex.length]

    seats.push({
      id: Number(`${room.id}${String(i + 1).padStart(3, '0')}`),
      study_room_id: room.id,
      seat_code: seatCode,
      display_label: `${String.fromCharCode(65 + rowIndex)}排${visualCol}座`,
      seat_type: i % 9 === 0 ? 'booth' : i % 4 === 0 ? 'high-desk' : 'standard',
      row_no: rowIndex + 1,
      col_no: visualCol,
      has_power: colIndex % 2 === 0,
      is_window_side: rowIndex === 0 || rowIndex === rows - 1,
      is_accessible: i % 17 === 0,
      seat_status: status,
      is_bookable: status === 'active' && i % 7 !== 0,
      map_x: baseX,
      map_y: baseY,
      map_width: seatWidth,
      map_height: seatHeight,
      map_rotation: i % 5 === 0 ? -6 : i % 5 === 3 ? 6 : 0,
      metadata_json: null,
    })
  }

  return {
    id: room.id * 10,
    study_room_id: room.id,
    version_no: 1,
    map_status: room.room_status === 'active' ? 'published' : 'draft',
    background_url: '',
    map_width: mapWidth,
    map_height: mapHeight,
    published_at: room.room_status === 'active' ? '2026-04-28 10:00:00' : null,
    created_by: 1,
    seats,
  }
}

function visibilityLabel(value) {
  return { public: '全校', organization: '机构', custom: '自定义' }[value] ?? value
}

function visibilityType(value) {
  return { public: 'success', organization: 'warning', custom: 'primary' }[value] ?? 'info'
}

function mapStatusText(value) {
  return { draft: '草稿', published: '已发布', archived: '已归档' }[value] ?? '-'
}

function mapStatusType(value) {
  return { draft: 'info', published: 'success', archived: 'warning' }[value] ?? 'info'
}

function seatStatusText(value) {
  return { active: '正常', inactive: '停用', maintenance: '维护中' }[value] ?? value
}

function seatStatusType(value) {
  return { active: 'success', inactive: 'info', maintenance: 'warning' }[value] ?? 'info'
}

function onStatusChange(room) {
  ElMessage.success(`《${room.room_name}》已${room.room_status === 'active' ? '启用' : '停用'}`)
}

function openDialog(room = null) {
  editingRoom.value = room
  Object.assign(form, room ? { ...room } : defaultForm())
  dialogVisible.value = true
}

function saveRoom() {
  if (!form.room_code || !form.room_name || !form.building) {
    ElMessage.warning('请填写房间编码、名称和楼栋')
    return
  }

  if (editingRoom.value) {
    Object.assign(editingRoom.value, { ...form })
    ElMessage.success('自习室信息已更新')
  } else {
    const newRoom = { id: Date.now(), ...form, created_at: new Date().toISOString().slice(0, 10) }
    rooms.value.push(newRoom)
    seatMapsByRoom[newRoom.id] = buildMockSeatMap(newRoom, rooms.value.length)
    ElMessage.success('自习室已创建')
  }

  dialogVisible.value = false
}

function viewSeatMap(room) {
  if (!seatMapsByRoom[room.id]) {
    seatMapsByRoom[room.id] = buildMockSeatMap(room, rooms.value.findIndex((item) => item.id === room.id))
  }
  currentRoom.value = room
  currentSeatMap.value = seatMapsByRoom[room.id]
  layoutMode.value = 'view'
  addSeatMode.value = false
  selectedSeat.value = null
  seatStatusFilter.value = ''
  onlyBookable.value = false
  Object.assign(seatForm, defaultSeatForm())
  syncMapForm()
  seatMapVisible.value = true
}

function syncMapForm() {
  if (!currentSeatMap.value) return
  Object.assign(mapForm, {
    background_url: currentSeatMap.value.background_url ?? '',
    map_width: currentSeatMap.value.map_width,
    map_height: currentSeatMap.value.map_height,
    map_status: currentSeatMap.value.map_status,
  })
}

function seatStyle(seat) {
  return {
    left: `${seat.map_x}px`,
    top: `${seat.map_y}px`,
    width: `${seat.map_width}px`,
    height: `${seat.map_height}px`,
    transform: `rotate(${seat.map_rotation}deg)`,
  }
}

function selectSeat(seat) {
  selectedSeat.value = seat
  Object.assign(seatForm, {
    seat_code: seat.seat_code,
    display_label: seat.display_label,
    seat_type: seat.seat_type,
    row_no: seat.row_no,
    col_no: seat.col_no,
    has_power: Boolean(seat.has_power),
    is_window_side: Boolean(seat.is_window_side),
    is_accessible: Boolean(seat.is_accessible),
    seat_status: seat.seat_status,
    is_bookable: Boolean(seat.is_bookable),
    map_x: Number(seat.map_x),
    map_y: Number(seat.map_y),
    map_width: Number(seat.map_width),
    map_height: Number(seat.map_height),
    map_rotation: Number(seat.map_rotation),
  })
}

function setSeatStatus(status) {
  seatForm.seat_status = status
}

function saveSeatConfig() {
  if (!selectedSeat.value) {
    ElMessage.warning('请先选择一个座位')
    return
  }

  Object.assign(selectedSeat.value, {
    seat_code: seatForm.seat_code.trim(),
    display_label: seatForm.display_label.trim(),
    seat_type: seatForm.seat_type,
    row_no: seatForm.row_no,
    col_no: seatForm.col_no,
    has_power: seatForm.has_power,
    is_window_side: seatForm.is_window_side,
    is_accessible: seatForm.is_accessible,
    seat_status: seatForm.seat_status,
    is_bookable: seatForm.is_bookable,
    map_x: seatForm.map_x,
    map_y: seatForm.map_y,
    map_width: seatForm.map_width,
    map_height: seatForm.map_height,
    map_rotation: seatForm.map_rotation,
  })

  markMapDraft()
  ElMessage.success(`座位 ${selectedSeat.value.seat_code} 设置已保存`)
}

function applyMapSettings() {
  if (!currentSeatMap.value) return
  currentSeatMap.value.background_url = mapForm.background_url.trim()
  currentSeatMap.value.map_width = mapForm.map_width
  currentSeatMap.value.map_height = mapForm.map_height
  currentSeatMap.value.map_status = mapForm.map_status
  ElMessage.success('地图配置已应用')
}

function saveLayoutDraft() {
  if (!currentSeatMap.value) return
  applyMapSettings()
  currentSeatMap.value.map_status = 'draft'
  mapForm.map_status = 'draft'
  ElMessage.success('布局草稿已保存')
}

function publishCurrentMap() {
  if (!currentSeatMap.value) return
  applyMapSettings()
  currentSeatMap.value.map_status = 'published'
  currentSeatMap.value.published_at = new Date().toISOString().slice(0, 19).replace('T', ' ')
  mapForm.map_status = 'published'
  ElMessage.success('当前布局版本已发布')
}

function createNewMapVersion() {
  if (!currentSeatMap.value) return
  const clonedSeats = currentSeatMap.value.seats.map((seat) => ({ ...seat, id: Date.now() + Math.floor(Math.random() * 1000) }))
  currentSeatMap.value = {
    ...currentSeatMap.value,
    id: Date.now(),
    version_no: currentSeatMap.value.version_no + 1,
    map_status: 'draft',
    published_at: null,
    seats: clonedSeats,
  }
  seatMapsByRoom[currentRoom.value.id] = currentSeatMap.value
  syncMapForm()
  selectedSeat.value = null
  addSeatMode.value = false
  ElMessage.success('已基于当前布局创建新的草稿版本')
}

function toggleAddSeatMode() {
  addSeatMode.value = !addSeatMode.value
}

function onMapStageClick(event) {
  if (layoutMode.value !== 'edit' || !addSeatMode.value || !currentSeatMap.value || !mapStageRef.value) return

  const rect = mapStageRef.value.getBoundingClientRect()
  const x = Math.max(20, Math.min(event.clientX - rect.left - 22, currentSeatMap.value.map_width - 70))
  const y = Math.max(40, Math.min(event.clientY - rect.top - 22, currentSeatMap.value.map_height - 70))
  const nextIndex = currentSeatMap.value.seats.length + 1
  const nextRow = Math.floor((nextIndex - 1) / 10) + 1
  const nextCol = ((nextIndex - 1) % 10) + 1
  const seatCode = `${String.fromCharCode(64 + Math.min(nextRow, 26))}${String(nextCol).padStart(2, '0')}`
  const newSeat = {
    id: Date.now(),
    study_room_id: currentRoom.value.id,
    seat_code: seatCode,
    display_label: `新座位 ${nextIndex}`,
    seat_type: 'standard',
    row_no: nextRow,
    col_no: nextCol,
    has_power: false,
    is_window_side: false,
    is_accessible: false,
    seat_status: 'active',
    is_bookable: true,
    map_x: Math.round(x),
    map_y: Math.round(y),
    map_width: 44,
    map_height: 44,
    map_rotation: 0,
    metadata_json: null,
  }

  currentSeatMap.value.seats.push(newSeat)
  currentRoom.value.total_capacity = currentSeatMap.value.seats.length
  selectSeat(newSeat)
  markMapDraft()
  addSeatMode.value = false
  ElMessage.success('新座位已添加到布局草稿')
}

function nudgeSeat(dx, dy) {
  seatForm.map_x = Math.max(0, seatForm.map_x + dx)
  seatForm.map_y = Math.max(0, seatForm.map_y + dy)
}

function duplicateSeat() {
  if (!selectedSeat.value || !currentSeatMap.value) return
  const cloned = {
    ...selectedSeat.value,
    id: Date.now(),
    seat_code: `${selectedSeat.value.seat_code}-C`,
    display_label: `${selectedSeat.value.display_label} 副本`,
    map_x: Number(selectedSeat.value.map_x) + 56,
    map_y: Number(selectedSeat.value.map_y) + 10,
  }
  currentSeatMap.value.seats.push(cloned)
  currentRoom.value.total_capacity = currentSeatMap.value.seats.length
  selectSeat(cloned)
  markMapDraft()
  ElMessage.success('已复制一个座位')
}

function removeSeat() {
  if (!selectedSeat.value || !currentSeatMap.value) return
  currentSeatMap.value.seats = currentSeatMap.value.seats.filter((seat) => seat.id !== selectedSeat.value.id)
  currentRoom.value.total_capacity = currentSeatMap.value.seats.length
  selectedSeat.value = null
  Object.assign(seatForm, defaultSeatForm())
  markMapDraft()
  ElMessage.success('座位已从当前布局中删除')
}

function markMapDraft() {
  if (!currentSeatMap.value) return
  currentSeatMap.value.map_status = 'draft'
  mapForm.map_status = 'draft'
}

async function deleteRoom(room) {
  try {
    await ElMessageBox.confirm(`确定要删除《${room.room_name}》吗？`, '删除确认', { type: 'warning' })
    rooms.value = rooms.value.filter((item) => item.id !== room.id)
    delete seatMapsByRoom[room.id]
    ElMessage.success('自习室已删除')
  } catch {}
}
</script>

<style scoped>
/* ── Page shell ── */
.rooms-page {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

/* ── Page Header ── */
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-header-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 800;
  color: #0f172a;
}

.title-icon {
  color: #003893;
}

.page-header-sub {
  margin-top: 5px;
  font-size: 13px;
  color: #94a3b8;
}

/* ── Stats Row ── */
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 18px 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f2f8;
}

.stat-icon-wrap {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.stat-blue  { background: #eff3ff; color: #003893; }
.stat-green { background: #f0fdf4; color: #16a34a; }
.stat-purple{ background: #f5f3ff; color: #7c3aed; }
.stat-orange{ background: #fff7ed; color: #ea580c; }

.stat-value {
  font-size: 26px;
  font-weight: 800;
  color: #0f172a;
  line-height: 1;
}

.stat-label {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

/* ── Filter Bar ── */
.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 14px 18px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f2f8;
}

.filter-bar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-count {
  font-size: 13px;
  color: #94a3b8;
  white-space: nowrap;
}

/* ── Content Card ── */
.content-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f2f8;
}

/* ── Pagination ── */
.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid #f0f2f8;
}

.code-text {
  font-family: 'SF Mono', Consolas, monospace;
  font-size: 12.5px;
  color: #2563eb;
  font-weight: 600;
}

.room-name {
  font-size: 13.5px;
  color: #111827;
  font-weight: 600;
  line-height: 1.4;
}

.room-alias {
  font-size: 11.5px;
  color: #9ca3af;
  margin-top: 2px;
}

.location-line {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.location-detail {
  font-size: 13px;
  color: #4b5563;
}

.capacity-text {
  font-size: 14px;
  font-weight: 700;
  color: #374151;
}

.text-muted {
  font-size: 12px;
  color: #9ca3af;
}

:deep(.el-divider--vertical) {
  margin: 0 2px;
  border-color: #e5e7eb;
}

.seat-dialog-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.seat-dialog-title {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 700;
  color: #111827;
}

.seat-dialog-meta {
  margin-top: 6px;
  font-size: 12.5px;
  color: #6b7280;
}

.meta-divider {
  margin: 0 8px;
  color: #d1d5db;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.map-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.map-toolbar-left,
.map-toolbar-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.summary-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 999px;
  background: #f8fafc;
  color: #475569;
  font-size: 13px;
}

.summary-chip strong {
  color: #111827;
}

.active-chip {
  background: #dcfce7;
}

.inactive-chip {
  background: #eef2f7;
}

.maintenance-chip {
  background: #fef3c7;
}

.bookable-chip {
  background: #dbeafe;
}

.filter-label {
  font-size: 13px;
  color: #6b7280;
}

.seat-map-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 360px;
  gap: 18px;
}

.map-stage-card,
.seat-side-panel {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
}

.map-stage-card {
  padding: 18px;
}

.seat-side-panel {
  padding: 18px;
}

.stage-title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.stage-title {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
}

.stage-subtitle {
  margin-top: 4px;
  font-size: 12px;
  color: #6b7280;
}

.edit-hint {
  font-size: 12px;
  color: #2563eb;
  background: #eff6ff;
  padding: 8px 10px;
  border-radius: 10px;
}

.room-front-banner {
  margin-bottom: 14px;
  padding: 10px 12px;
  border-radius: 12px;
  background: linear-gradient(90deg, #eff6ff 0%, #dbeafe 100%);
  color: #1d4ed8;
  font-size: 13px;
  font-weight: 600;
  text-align: center;
}

.map-stage-scroll {
  overflow: auto;
  padding-bottom: 6px;
}

.map-stage {
  position: relative;
  min-width: max-content;
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.96), rgba(248, 250, 252, 0.98)),
    repeating-linear-gradient(
      90deg,
      rgba(148, 163, 184, 0.08) 0,
      rgba(148, 163, 184, 0.08) 1px,
      transparent 1px,
      transparent 48px
    ),
    repeating-linear-gradient(
      0deg,
      rgba(148, 163, 184, 0.08) 0,
      rgba(148, 163, 184, 0.08) 1px,
      transparent 1px,
      transparent 48px
    );
  background-size: cover;
  background-position: center;
  border: 1px solid #dbe3f0;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.75);
}

.map-stage.editable {
  cursor: default;
}

.map-stage.placing {
  cursor: crosshair;
}

.window-band {
  position: absolute;
  top: 18px;
  left: 50%;
  transform: translateX(-50%);
  padding: 5px 12px;
  border-radius: 999px;
  background: rgba(59, 130, 246, 0.1);
  color: #2563eb;
  font-size: 12px;
  font-weight: 600;
}

.door-marker {
  position: absolute;
  right: 20px;
  bottom: 18px;
  padding: 5px 12px;
  border-radius: 10px;
  border: 1px dashed #f59e0b;
  background: rgba(245, 158, 11, 0.1);
  color: #b45309;
  font-size: 12px;
  font-weight: 600;
}

.seat-node {
  position: absolute;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border-radius: 10px;
  border: 1px solid transparent;
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease;
  color: #0f172a;
}

.seat-node:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 18px rgba(15, 23, 42, 0.12);
}

.seat-node.selected {
  outline: 3px solid rgba(37, 99, 235, 0.24);
  border-color: #2563eb;
  box-shadow: 0 10px 18px rgba(37, 99, 235, 0.2);
}

.seat-node.unbookable::after {
  content: '';
  position: absolute;
  inset: 4px;
  border-radius: 8px;
  border: 1px dashed rgba(15, 23, 42, 0.3);
}

.seat-active {
  background: #dcfce7;
  border-color: #86efac;
}

.seat-inactive {
  background: #e5e7eb;
  border-color: #cbd5e1;
}

.seat-maintenance {
  background: #fef3c7;
  border-color: #fcd34d;
}

.seat-node-label {
  font-size: 11px;
  font-weight: 700;
  line-height: 1;
}

.seat-node-code {
  font-size: 10px;
  line-height: 1;
  opacity: 0.72;
}

.seat-power-icon {
  position: absolute;
  top: 4px;
  right: 4px;
  font-size: 12px;
  color: #d97706;
}

.legend-row {
  display: flex;
  align-items: center;
  gap: 18px;
  flex-wrap: wrap;
  margin-top: 16px;
  padding-top: 14px;
  border-top: 1px solid #eef2f7;
  color: #6b7280;
  font-size: 12px;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.legend-dot {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1px solid transparent;
}

.legend-lock {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1px dashed #94a3b8;
}

.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #111827;
  margin-bottom: 14px;
}

.config-card {
  margin-bottom: 16px;
  padding: 14px;
  border-radius: 14px;
  background: #f8fafc;
}

.config-card-title {
  font-size: 13px;
  font-weight: 700;
  color: #334155;
  margin-bottom: 10px;
}

.seat-profile {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 14px;
  border-radius: 14px;
  background: #f8fafc;
  margin-bottom: 16px;
}

.seat-profile-code {
  font-size: 18px;
  font-weight: 700;
  color: #0f172a;
}

.seat-profile-name {
  margin-top: 4px;
  font-size: 13px;
  color: #64748b;
}

.seat-form :deep(.el-form-item),
.config-card :deep(.el-form-item) {
  margin-bottom: 14px;
}

.seat-form :deep(.el-form-item__label),
.config-card :deep(.el-form-item__label) {
  color: #64748b;
  font-size: 13px;
  padding-bottom: 6px;
}

.seat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.nudge-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  margin-bottom: 14px;
}

.feature-list {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px 12px;
  margin: 10px 0 18px;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}

.empty-seat-panel {
  min-height: 460px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #94a3b8;
  gap: 10px;
}

.empty-seat-panel p {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
}

.empty-seat-panel span {
  font-size: 12px;
}

.form-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 14px;
  padding-left: 8px;
  border-left: 3px solid #2563eb;
}

.dialog-form :deep(.el-form-item__label) {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
  padding-bottom: 4px;
}

.dialog-form :deep(.el-form-item) {
  margin-bottom: 16px;
}

.dialog-form :deep(.el-divider) {
  margin: 8px 0 16px;
}

.switch-row {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 32px;
}

.switch-label {
  font-size: 13px;
  color: #6b7280;
}

:deep(.room-dialog .el-dialog__body) {
  padding: 20px 28px 8px;
}

:deep(.room-dialog .el-dialog__footer) {
  padding: 12px 28px 20px;
}

:deep(.seat-map-dialog .el-dialog__body) {
  padding: 14px 18px 18px;
  background: #f8fafc;
}

@media (max-width: 1180px) {
  .seat-map-layout {
    grid-template-columns: 1fr;
  }
}
</style>

 