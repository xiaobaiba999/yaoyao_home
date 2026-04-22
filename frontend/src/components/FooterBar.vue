<template>
  <footer class="footer">
    <div class="footer__container">
      <span class="footer__text">© 2023 瑶瑶纪念空间</span>
      <svg class="footer__heart" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
        <path d="M12 21C12 21 3 13.5 3 8.5C3 5.42 5.42 3 8.5 3C10.24 3 11.91 3.81 12 5C12.09 3.81 13.76 3 15.5 3C18.58 3 21 5.42 21 8.5C21 13.5 12 21 12 21Z" />
      </svg>
      <span class="footer__text">用爱记录每一刻</span>
    </div>
  </footer>
</template>

<script setup>
</script>

<style scoped>
.footer {
  width: 100%;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 24px;
  box-sizing: border-box;
}

.footer__container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
}

.footer__text {
  font-size: 13px;
  font-weight: 300;
  color: var(--color-text-light);
  letter-spacing: 0.5px;
}

.footer__heart {
  width: 14px;
  height: 14px;
  color: var(--color-accent);
  flex-shrink: 0;
}

@media (max-width: 768px) {
  .footer {
    height: 50px;
  }

  .footer__text {
    font-size: 12px;
  }

  .footer__heart {
    width: 12px;
    height: 12px;
  }
}
</style>
