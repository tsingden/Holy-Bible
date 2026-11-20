import React, { useState } from 'react';
import {
  ChevronDown,
  ChevronUp,
  Code2,
  Copyright,
  ExternalLink,
  Gavel,
  Headphones,
  Info,
  Languages,
} from 'lucide-react';
import { CopyrightLicenseCategory, SystemLanguage } from '../types/bible';
import { BibleSourceAttribution, CreditEntryItem } from '../localization/appStrings';
import { useBibleApp } from '../context/BibleAppContext';
import { cssWithAlpha } from '../theme/themeTokens';
import { LiquidGlassSurface } from './LiquidGlassShell';

export const CopyrightAndCreditsSection: React.FC = () => {
  const { tokens, strings, preferences } = useBibleApp();
  const sysLang = preferences.systemLanguage;
  const headerFont =
    sysLang === SystemLanguage.NEPALI
      ? '"Noto Serif Devanagari", serif'
      : '"Playfair Display", Georgia, serif';

  const [mainExpanded, setMainExpanded] = useState(false);
  const [copyrightExpanded, setCopyrightExpanded] = useState(false);
  const [translationExpanded, setTranslationExpanded] = useState(false);
  const [audioExpanded, setAudioExpanded] = useState(false);
  const [developerExpanded, setDeveloperExpanded] = useState(false);

  return (
    <LiquidGlassSurface tokens={tokens} className="rounded-2xl p-4">
      <button
        type="button"
        onClick={() => setMainExpanded((v) => !v)}
        className="flex w-full items-center justify-between gap-3 text-left"
      >
        <div className="flex min-w-0 flex-1 items-center gap-2.5">
          <Info className="h-5 w-5 shrink-0" style={{ color: tokens.accentColor }} />
          <div className="min-w-0">
            <div
              className="text-[16px] font-bold"
              style={{ fontFamily: headerFont, color: tokens.textPrimary }}
            >
              {strings.licensesCardTitle}
            </div>
            <div className="text-[11.5px] leading-snug" style={{ color: tokens.textSecondary }}>
              {strings.authenticEditionsHeading}
            </div>
          </div>
        </div>
        {mainExpanded ? (
          <ChevronUp className="h-5 w-5 shrink-0" style={{ color: tokens.accentColor }} />
        ) : (
          <ChevronDown className="h-5 w-5 shrink-0" style={{ color: tokens.textSecondary }} />
        )}
      </button>

      {mainExpanded && (
        <div className="mt-3 space-y-2.5">
          <div className="h-[0.7px] w-full" style={{ backgroundColor: cssWithAlpha(tokens.borderTop, 0.5) }} />

          <SubAccordion
            icon={<Gavel className="h-4 w-4" />}
            title={strings.subsectionBibleCopyrightTitle}
            subtitle={strings.subsectionBibleCopyrightSubtitle}
            expanded={copyrightExpanded}
            onToggle={() => setCopyrightExpanded((v) => !v)}
          >
            <div className="space-y-2.5">
              {strings.bibleSourceAttributions.map((item) => (
                <BibleSourceAttributionCard key={item.versionId} item={item} />
              ))}
            </div>
          </SubAccordion>

          <SubAccordion
            icon={<Languages className="h-4 w-4" />}
            title={strings.subsectionBibleTranslationCreditsTitle}
            subtitle={strings.subsectionBibleTranslationCreditsSubtitle}
            expanded={translationExpanded}
            onToggle={() => setTranslationExpanded((v) => !v)}
          >
            <div className="space-y-2.5">
              {strings.bibleTranslationCreditItems.map((entry) => (
                <CreditDetailCard key={entry.title} entry={entry} />
              ))}
            </div>
          </SubAccordion>

          <SubAccordion
            icon={<Headphones className="h-4 w-4" />}
            title={strings.subsectionAudioMediaCreditsTitle}
            subtitle={strings.subsectionAudioMediaCreditsSubtitle}
            expanded={audioExpanded}
            onToggle={() => setAudioExpanded((v) => !v)}
          >
            <div className="space-y-2.5">
              {strings.audioMediaCreditItems.map((entry) => (
                <CreditDetailCard key={entry.title} entry={entry} />
              ))}
            </div>
          </SubAccordion>

          <SubAccordion
            icon={<Code2 className="h-4 w-4" />}
            title={strings.subsectionDeveloperTechCreditsTitle}
            subtitle={strings.subsectionDeveloperTechCreditsSubtitle}
            expanded={developerExpanded}
            onToggle={() => setDeveloperExpanded((v) => !v)}
          >
            <div className="space-y-2.5">
              {strings.developerTechCreditItems.map((entry) => (
                <CreditDetailCard key={entry.title} entry={entry} />
              ))}
            </div>
          </SubAccordion>
        </div>
      )}
    </LiquidGlassSurface>
  );
};

const SubAccordion: React.FC<{
  icon: React.ReactNode;
  title: string;
  subtitle: string;
  expanded: boolean;
  onToggle: () => void;
  children: React.ReactNode;
}> = ({ icon, title, subtitle, expanded, onToggle, children }) => {
  const { tokens } = useBibleApp();
  return (
    <div
      className="overflow-hidden rounded-2xl border p-3"
      style={{
        backgroundColor: cssWithAlpha(
          tokens.glassSurface,
          tokens.isDark ? 0.42 : 0.56
        ),
        borderColor: expanded ? cssWithAlpha(tokens.accentColor, 0.55) : tokens.borderTop,
      }}
    >
      <button
        type="button"
        onClick={onToggle}
        className="flex w-full items-center justify-between gap-2 text-left"
      >
        <div className="flex min-w-0 flex-1 items-center gap-2.5">
          <div
            className="flex h-8 w-8 shrink-0 items-center justify-center rounded-xl"
            style={{
              backgroundColor: cssWithAlpha(tokens.accentColor, 0.14),
              color: tokens.accentColor,
            }}
          >
            {icon}
          </div>
          <div className="min-w-0">
            <div className="text-[13.5px] font-bold" style={{ color: tokens.textPrimary }}>
              {title}
            </div>
            <div className="text-[11px] leading-tight" style={{ color: tokens.textSecondary }}>
              {subtitle}
            </div>
          </div>
        </div>
        {expanded ? (
          <ChevronUp className="h-4 w-4 shrink-0" style={{ color: tokens.accentColor }} />
        ) : (
          <ChevronDown className="h-4 w-4 shrink-0" style={{ color: tokens.textSecondary }} />
        )}
      </button>

      {expanded && <div className="mt-2.5">{children}</div>}
    </div>
  );
};

const BibleSourceAttributionCard: React.FC<{ item: BibleSourceAttribution }> = ({ item }) => {
  const { tokens, strings } = useBibleApp();
  const badgeColor =
    item.licenseCategory === CopyrightLicenseCategory.OPEN_LICENSE
      ? '#1B8A5A'
      : item.licenseCategory === CopyrightLicenseCategory.PUBLIC_DOMAIN
      ? '#2563EB'
      : item.licenseCategory === CopyrightLicenseCategory.COPYRIGHTED
      ? '#C2410C'
      : tokens.textSecondary;

  return (
    <div
      className="rounded-xl border p-3"
      style={{
        backgroundColor: tokens.glassSurfaceElevated,
        borderColor: tokens.borderTop,
      }}
    >
      <div className="flex flex-wrap items-start justify-between gap-2">
        <span className="min-w-0 flex-1 text-[13.5px] font-bold" style={{ color: tokens.textPrimary }}>
          {item.title}
        </span>
        <span
          className="rounded-lg px-2 py-0.5 text-[10px] font-bold"
          style={{
            backgroundColor: cssWithAlpha(badgeColor, 0.15),
            color: badgeColor,
          }}
        >
          {strings.copyrightCategoryLabel(item.licenseCategory)}
        </span>
      </div>

      <div className="mt-2 space-y-1.5">
        <LabeledField label={strings.versionNameAbbrevLabel} value={item.versionId} />
        <LabeledField label={strings.fullTranslationNameLabel} value={item.fullTranslationName} />
        <LabeledField label={strings.copyrightOwnerLabel} value={item.ownerPublisher} />
        <LabeledField label={strings.copyrightNoticeLabel} value={item.copyrightNotice} />
        <LabeledField label={strings.publisherCreditLabel} value={item.translationCredits} />
        <LabeledField label={strings.licenseLabel} value={item.license} />
        <LabeledField label={strings.usagePermissionsLabel} value={item.usagePermissions} />
      </div>

      {item.sourceUrls.length > 0 && (
        <div className="mt-2">
          <div className="text-[10.5px] font-bold" style={{ color: tokens.accentColor }}>
            {strings.sourceLabel}
          </div>
          <div className="mt-1 flex flex-wrap gap-1.5">
            {item.sourceUrls.map((url) => (
              <a
                key={url}
                href={url}
                target="_blank"
                rel="noopener noreferrer"
                className="inline-flex items-center gap-1 rounded-lg border px-2 py-1 text-[11px] font-medium hover:underline"
                style={{
                  backgroundColor: cssWithAlpha(tokens.accentColor, 0.12),
                  borderColor: cssWithAlpha(tokens.accentColor, 0.35),
                  color: tokens.accentColor,
                }}
              >
                <ExternalLink className="h-3 w-3 shrink-0" />
                <span className="max-w-[220px] truncate">{url}</span>
              </a>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

const CreditDetailCard: React.FC<{ entry: CreditEntryItem }> = ({ entry }) => {
  const { tokens } = useBibleApp();
  return (
    <div
      className="rounded-xl border p-3"
      style={{
        backgroundColor: tokens.glassSurfaceElevated,
        borderColor: tokens.borderTop,
      }}
    >
      <div className="flex items-center gap-1.5">
        <Copyright className="h-4 w-4 shrink-0" style={{ color: tokens.accentColor }} />
        <span className="text-[13px] font-bold" style={{ color: tokens.textPrimary }}>
          {entry.title}
        </span>
      </div>
      <div className="mt-0.5 text-[11px] font-medium" style={{ color: tokens.accentColor }}>
        {entry.subtitle}
      </div>
      <p
        className="mt-1.5 whitespace-pre-line text-xs leading-relaxed"
        style={{ color: tokens.textSecondary }}
      >
        {entry.description}
      </p>
      {entry.links && entry.links.length > 0 && (
        <div className="mt-2 flex flex-wrap gap-1.5">
          {entry.links.map((link) => (
            <a
              key={link.url}
              href={link.url}
              target="_blank"
              rel="noopener noreferrer"
              className="inline-flex items-center gap-1 rounded-lg border px-2 py-1 text-[11px] font-medium hover:underline"
              style={{
                backgroundColor: cssWithAlpha(tokens.accentColor, 0.12),
                borderColor: cssWithAlpha(tokens.accentColor, 0.35),
                color: tokens.accentColor,
              }}
            >
              <ExternalLink className="h-3 w-3 shrink-0" />
              <span>{link.label}</span>
            </a>
          ))}
        </div>
      )}
    </div>
  );
};

const LabeledField: React.FC<{ label: string; value: string }> = ({ label, value }) => {
  const { tokens } = useBibleApp();
  return (
    <div>
      <div className="text-[10.5px] font-bold" style={{ color: tokens.accentColor }}>
        {label}
      </div>
      <div className="text-[12px] leading-snug" style={{ color: tokens.textPrimary }}>
        {value}
      </div>
    </div>
  );
};
