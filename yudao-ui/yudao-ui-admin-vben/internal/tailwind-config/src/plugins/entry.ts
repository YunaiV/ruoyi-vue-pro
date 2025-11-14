import plugin from 'tailwindcss/plugin.js';

const enterAnimationPlugin = plugin(({ addUtilities }) => {
  const maxChild = 5;
  const utilities: Record<string, any> = {};
  for (let i = 1; i <= maxChild; i++) {
    const baseDelay = 0.1;
    const delay = `${baseDelay * i}s`;

    utilities[`.enter-x:nth-child(${i})`] = {
      animation: `enter-x-animation 0.3s ease-in-out ${delay} forwards`,
      opacity: '0',
      transform: `translateX(50px)`,
    };

    utilities[`.enter-y:nth-child(${i})`] = {
      animation: `enter-y-animation 0.3s ease-in-out ${delay} forwards`,
      opacity: '0',
      transform: `translateY(50px)`,
    };

    utilities[`.-enter-x:nth-child(${i})`] = {
      animation: `enter-x-animation 0.3s ease-in-out ${delay} forwards`,
      opacity: '0',
      transform: `translateX(-50px)`,
    };

    utilities[`.-enter-y:nth-child(${i})`] = {
      animation: `enter-y-animation 0.3s ease-in-out ${delay} forwards`,
      opacity: '0',
      transform: `translateY(-50px)`,
    };
  }

  // 添加动画关键帧
  addUtilities(utilities);
  addUtilities({
    '@keyframes enter-x-animation': {
      to: {
        opacity: '1',
        transform: 'translateX(0)',
      },
    },
    '@keyframes enter-y-animation': {
      to: {
        opacity: '1',
        transform: 'translateY(0)',
      },
    },
  });
});

export { enterAnimationPlugin };
