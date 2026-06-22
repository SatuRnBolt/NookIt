<template>
  <div class="phone-screen">
    <!-- phone-content clips page scroll, popups 通过 teleport 渲染在 phone-screen 层 -->
    <div class="phone-content">
      <router-view />
    </div>
  </div>
</template>

<style>
body {
  margin: 0;
  background: #e8eaf2;
  min-height: 100dvh;
  display: flex;
  justify-content: center;
  align-items: center;
}

.phone-screen {
  position: relative;
  width: 390px;
  height: min(100dvh, 844px);
  background: #f4f6fb;
  /* transform 让此元素成为 position:fixed 子元素的 containing block */
  transform: translate3d(0, 0, 0);
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15), 0 0 0 1px #dde1ee;
  border-radius: 8px;
  flex-shrink: 0;
}

.phone-content {
  width: 100%;
  height: 100%;
  overflow: hidden;
}

@media (max-width: 440px) {
  body { align-items: flex-start; }
  .phone-screen {
    width: 100vw;
    height: 100dvh;
    border-radius: 0;
    box-shadow: none;
  }
}
</style>
