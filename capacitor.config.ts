import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.budget.client',
  appName: 'budget-client',
  webDir: 'dist',
  server: {
    androidScheme: 'https',
    allowNavigation: [ "*.glaubhanta.site"],
  
    cleartext: true
  },
  plugins: {
    CapacitorHttp: {
      enabled: true,
    },
  }
};

export default config;
