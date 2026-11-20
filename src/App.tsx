import React from 'react';
import { BookOpen, Bookmark, Home, Search, Settings } from 'lucide-react';
import { BibleAppProvider, useBibleApp } from './context/BibleAppContext';
import {
  LiquidGlassBackdrop,
  LiquidGlassBottomNav,
  NavItemSpec,
  ToastBanner,
} from './components/LiquidGlassShell';
import { ScripturePickerSheet } from './components/ScripturePickerSheet';
import { LanguageSelectorSheet } from './components/LanguageSelectorSheet';
import { HomeScreen } from './screens/HomeScreen';
import { ReaderScreen } from './screens/ReaderScreen';
import { SearchScreen } from './screens/SearchScreen';
import { LibraryScreen } from './screens/LibraryScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { DownloadsScreen } from './screens/DownloadsScreen';
import { ThemeStudioScreen } from './screens/ThemeStudioScreen';

const AppShell: React.FC = () => {
  const {
    route,
    navigate,
    strings,
    tokens,
    preferences,
    readerState,
    showPickerDialog,
    pickerInitialTestament,
    closeScripturePicker,
    showLanguageSheet,
    closeLanguageSheet,
    openChapter,
  } = useBibleApp();

  const showChromeNav =
    route !== 'reader' && route !== 'downloads' && route !== 'theme_studio';

  const navItems: NavItemSpec[] = [
    { route: 'home', label: strings.navHome, icon: <Home className="h-5 w-5" /> },
    { route: 'reader', label: strings.navBible, icon: <BookOpen className="h-5 w-5" /> },
    { route: 'search', label: strings.navSearch, icon: <Search className="h-5 w-5" /> },
    { route: 'library', label: strings.navSaved, icon: <Bookmark className="h-5 w-5" /> },
    { route: 'settings', label: strings.navSettings, icon: <Settings className="h-5 w-5" /> },
  ];

  return (
    <LiquidGlassBackdrop tokens={tokens} className="h-full w-full">
      <div className="flex h-full w-full flex-col overflow-hidden">
        {route === 'home' && <HomeScreen />}
        {route === 'reader' && <ReaderScreen />}
        {route === 'search' && <SearchScreen />}
        {route === 'library' && <LibraryScreen />}
        {route === 'settings' && <SettingsScreen />}
        {route === 'downloads' && <DownloadsScreen />}
        {route === 'theme_studio' && <ThemeStudioScreen />}
      </div>

      {showChromeNav && (
        <LiquidGlassBottomNav
          items={navItems}
          currentRoute={route}
          onNavigate={navigate}
        />
      )}

      <ScripturePickerSheet
        open={showPickerDialog}
        initialBookId={
          pickerInitialTestament === 'NT'
            ? 40
            : pickerInitialTestament === 'OT'
            ? 1
            : readerState.currentBook?.id ?? preferences.lastBookId
        }
        initialChapter={
          pickerInitialTestament ? 1 : readerState.currentChapter || preferences.lastChapter
        }
        initialTestamentFilter={pickerInitialTestament}
        onSelectLocation={(bookId, ch, v) => {
          closeScripturePicker();
          void openChapter(bookId, ch, v);
          navigate('reader');
        }}
        onDismiss={closeScripturePicker}
      />

      <LanguageSelectorSheet
        open={showLanguageSheet}
        onOpenDownloadsManager={() => navigate('downloads')}
        onDismiss={closeLanguageSheet}
      />

      <ToastBanner />
    </LiquidGlassBackdrop>
  );
};

export function App(): React.ReactElement {
  return (
    <BibleAppProvider>
      <AppShell />
    </BibleAppProvider>
  );
}

export default App;
