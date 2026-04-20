<template>
  <div class="love-timeline">
    <div class="timeline-container">
      <div class="timeline-line"></div>
      <div 
        v-for="(item, index) in timelineData" 
        :key="index" 
        class="timeline-item"
      >
        <div class="timeline-node"></div>
        <div class="timeline-content">
          <div class="timeline-header">
            <span class="timeline-date">{{ item.date }}</span>
            <span class="timeline-title">{{ item.title }}</span>
          </div>
          <p class="timeline-description">{{ item.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
const timelineData = [
  { date: '2023.02.28', title: '初次相遇', description: '在那个特别的日子，我们第一次相见' },
  { date: '2023.05.20', title: '确定关系', description: '520，我们正式在一起了' },
  { date: '2023.08.15', title: '第一次旅行', description: '一起去了海边，留下了美好的回忆' },
  { date: '2024.02.14', title: '情人节', description: '一起度过了第一个情人节' },
]
</script>

<style scoped>
.love-timeline {
  width: 100%;
  padding: 40px 20px;
}

.timeline-container {
  position: relative;
  max-width: 600px;
  margin: 0 auto;
  padding-left: 30px;
}

.timeline-line {
  position: absolute;
  left: 5px;
  top: 0;
  bottom: 0;
  width: 2px;
  background-color: var(--color-border);
}

.timeline-item {
  position: relative;
  padding-bottom: 40px;
}

.timeline-item:last-child {
  padding-bottom: 0;
}

.timeline-node {
  position: absolute;
  left: -25px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: var(--color-accent);
}

.timeline-content {
  padding-left: 10px;
}

.timeline-header {
  margin-bottom: 8px;
}

.timeline-date {
  font-size: 13px;
  color: var(--color-text-light);
  margin-right: 12px;
}

.timeline-title {
  font-size: 15px;
  font-weight: 500;
  color: var(--color-title);
}

.timeline-description {
  font-size: 14px;
  color: var(--color-text);
  line-height: 1.6;
  margin: 0;
}
</style>
