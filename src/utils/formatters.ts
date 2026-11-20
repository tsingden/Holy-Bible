import {
  BIBLE_LANGUAGE_META,
  BibleBook,
  BibleLanguage,
  BibleVerse,
  SystemLanguage,
  getBookNameForLanguage,
  getVerseTextForLanguage,
} from '../types/bible';

const DEVANAGARI_DIGITS = ['०', '१', '२', '३', '४', '५', '६', '७', '८', '९'];

export function toDevanagari(num: number): string {
  return toDevanagariDigits(String(Math.trunc(num)));
}

export function toDevanagariDigits(raw: string): string {
  let out = '';
  for (let i = 0; i < raw.length; i++) {
    const ch = raw.charAt(i);
    if (ch >= '0' && ch <= '9') {
      out += DEVANAGARI_DIGITS[ch.charCodeAt(0) - 48];
    } else {
      out += ch;
    }
  }
  return out;
}

export function formatNumber(value: number, language: SystemLanguage): string {
  const s = String(Math.trunc(value));
  return language === SystemLanguage.NEPALI ? toDevanagariDigits(s) : s;
}

export function formatDecimal(value: number, language: SystemLanguage, decimals = 2): string {
  const raw = value.toFixed(decimals);
  return language === SystemLanguage.NEPALI ? toDevanagariDigits(raw) : raw;
}

export function formatPercent(percent: number, language: SystemLanguage): string {
  const s = String(Math.trunc(percent));
  return language === SystemLanguage.NEPALI ? `${toDevanagariDigits(s)}%` : `${s}%`;
}

export function formatSpeed(speed: number, language: SystemLanguage): string {
  const formatted = `${speed.toFixed(2)}x`;
  return language === SystemLanguage.NEPALI ? toDevanagariDigits(formatted) : formatted;
}

export function formatBytes(bytes: number, language: SystemLanguage): string {
  if (bytes <= 0) {
    return language === SystemLanguage.NEPALI ? '० MB' : '0 MB';
  }
  const kb = bytes / 1024;
  const mb = kb / 1024;
  if (mb >= 0.1) {
    const s = mb.toFixed(1);
    return language === SystemLanguage.NEPALI ? `${toDevanagariDigits(s)} MB` : `${s} MB`;
  }
  const kbInt = Math.max(1, Math.round(kb));
  return language === SystemLanguage.NEPALI ? `${toDevanagari(kbInt)} KB` : `${kbInt} KB`;
}

export function formatDateTime(epochMillis: number, language: SystemLanguage): string {
  const d = new Date(epochMillis);
  const pad2 = (n: number) => String(n).padStart(2, '0');
  const yr = d.getFullYear();
  const mo = pad2(d.getMonth() + 1);
  const da = pad2(d.getDate());
  const hr = pad2(d.getHours());
  const mi = pad2(d.getMinutes());
  if (language === SystemLanguage.NEPALI) {
    return `${toDevanagariDigits(String(yr))}-${toDevanagariDigits(mo)}-${toDevanagariDigits(da)}, ${toDevanagariDigits(hr)}:${toDevanagariDigits(mi)}`;
  }
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
  return `${months[d.getMonth()]} ${d.getDate()}, ${pad2(d.getHours())}:${mi}`;
}

export function formatFullDate(epochMillis: number, language: SystemLanguage): string {
  const d = new Date(epochMillis);
  if (language === SystemLanguage.NEPALI) {
    const weekdays = ['आइतबार', 'सोमबार', 'मंगलबार', 'बुधबार', 'बिहीबार', 'शुक्रबार', 'शनिबार'];
    const months = ['जनवरी', 'फेब्रुअरी', 'मार्च', 'अप्रिल', 'मे', 'जुन', 'जुलाई', 'अगस्ट', 'सेप्टेम्बर', 'अक्टोबर', 'नोभेम्बर', 'डिसेम्बर'];
    const h24 = d.getHours();
    const period =
      h24 < 12 ? 'बिहान' : h24 < 17 ? 'दिउँसो' : h24 < 20 ? 'साँझ' : 'राति';
    const h12 = h24 % 12 === 0 ? 12 : h24 % 12;
    const min = String(d.getMinutes()).padStart(2, '0');
    return `${weekdays[d.getDay()]}, ${months[d.getMonth()]} ${toDevanagari(d.getDate())}, ${toDevanagari(d.getFullYear())} • ${period} ${toDevanagari(h12)}:${toDevanagariDigits(min)}`;
  }
  return d.toLocaleString('en-US', {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

export function formatVerseShareText(
  book: BibleBook,
  verse: BibleVerse,
  primaryLanguage: BibleLanguage,
  secondaryLanguage: BibleLanguage,
  appBrand = 'Holy Bible'
): string {
  const lines: string[] = [];

  const primaryMeta = BIBLE_LANGUAGE_META[primaryLanguage];
  const primaryChap = primaryMeta.usesDevanagari ? toDevanagari(verse.chapter) : String(verse.chapter);
  const primaryVerseNum = primaryMeta.usesDevanagari ? toDevanagari(verse.verse) : String(verse.verse);
  const primaryRef = `${getBookNameForLanguage(book, primaryLanguage)} ${primaryChap}:${primaryVerseNum}`;
  const primaryText = getVerseTextForLanguage(verse, primaryLanguage);

  lines.push(`${primaryRef} (${primaryMeta.shortBadge})`);
  if (primaryText.trim()) {
    lines.push(`"${primaryText}"`);
  }

  if (secondaryLanguage !== primaryLanguage) {
    const secondaryMeta = BIBLE_LANGUAGE_META[secondaryLanguage];
    const secChap = secondaryMeta.usesDevanagari ? toDevanagari(verse.chapter) : String(verse.chapter);
    const secVerseNum = secondaryMeta.usesDevanagari ? toDevanagari(verse.verse) : String(verse.verse);
    const secRef = `${getBookNameForLanguage(book, secondaryLanguage)} ${secChap}:${secVerseNum}`;
    const secText = getVerseTextForLanguage(verse, secondaryLanguage);
    if (secText.trim()) {
      lines.push('');
      lines.push(`${secRef} (${secondaryMeta.shortBadge})`);
      lines.push(`"${secText}"`);
    }
  }

  lines.push('');
  lines.push(`— ${appBrand}`);
  return lines.join('\n');
}

export async function copyTextToClipboard(text: string): Promise<boolean> {
  try {
    if (navigator.clipboard && window.isSecureContext) {
      await navigator.clipboard.writeText(text);
      return true;
    }
  } catch {
    // Fallback below
  }
  try {
    const ta = document.createElement('textarea');
    ta.value = text;
    ta.style.position = 'fixed';
    ta.style.opacity = '0';
    document.body.appendChild(ta);
    ta.focus();
    ta.select();
    const ok = document.execCommand('copy');
    document.body.removeChild(ta);
    return ok;
  } catch {
    return false;
  }
}

export function buildWhatsAppUrl(text: string): string {
  return `https://wa.me/?text=${encodeURIComponent(text)}`;
}
