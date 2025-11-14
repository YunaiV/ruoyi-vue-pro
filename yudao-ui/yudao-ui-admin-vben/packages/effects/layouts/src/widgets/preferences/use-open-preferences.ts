import { ref } from 'vue';

const openPreferences = ref(false);

function useOpenPreferences() {
  function handleOpenPreference() {
    openPreferences.value = true;
  }

  return {
    handleOpenPreference,
    openPreferences,
  };
}

export { useOpenPreferences };
