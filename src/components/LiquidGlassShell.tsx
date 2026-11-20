import React from 'react';
import {
  AppRoute,
  ButtonStyleOption,
  NavBarAppearance,
} from '../types/bible';
import { LiquidGlassTokens, argbToCss, cssWithAlpha } from '../theme/themeTokens';
import { useBibleApp } from '../context/BibleAppContext';

export const LiquidGlassBackdrop: React.FC<{
  tokens?: LiquidGlassTokens;
  className?: string;
  children: React.ReactNode;
}> = ({ tokens: overrideTokens, className = '', children }) => {
  const app = useBibleApp();
  const tokens = overrideTokens ?? app.tokens;

  const blurPx = tokens.blurIntensityDp > 0.5 ? Math.min(48, tokens.blurIntensityDp * 1.4) : 0;

  return (
    <div
      className={`relative overflow-hidden ${className}`}
      style={{
        background: `linear-gradient(180deg, ${tokens.backdropStart} 0%, ${tokens.backdropEnd} 100%)`,
        color: tokens.textPrimary,
      }}
    >
      {/* Ambient Radial Glow Orbs + Crystal Sheen */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 overflow-hidden"
        style={{
          filter: blurPx > 0 ? `blur(${blurPx}px)` : undefined,
        }}
      >
        <div
          className="absolute -left-[15%] -top-[10%] h-[55vw] max-h-[520px] w-[55vw] max-w-[520px] rounded-full"
          style={{
            background: `radial-gradient(circle, ${tokens.orbPrimary} 0%, transparent 70%)`,
          }}
        />
        <div
          className="absolute -right-[15%] top-[20%] h-[60vw] max-h-[560px] w-[60vw] max-w-[560px] rounded-full"
          style={{
            background: `radial-gradient(circle, ${tokens.orbSecondary} 0%, transparent 70%)`,
          }}
        />
        <div
          className="absolute bottom-[-12%] left-[12%] h-[52vw] max-h-[500px] w-[52vw] max-w-[500px] rounded-full"
          style={{
            background: `radial-gradient(circle, ${tokens.orbTertiary} 0%, transparent 70%)`,
          }}
        />
        {tokens.isCrystalClear && (
          <div
            className="absolute inset-0"
            style={{
              background: `linear-gradient(142deg, transparent 22%, ${
                tokens.isDark ? 'rgba(255,255,255,0.08)' : 'rgba(255,255,255,0.38)'
              } 44%, ${cssWithAlpha(tokens.accentColor, tokens.isDark ? 0.14 : 0.12)} 54%, transparent 72%)`,
            }}
          />
        )}
      </div>

      <div className="relative z-10 flex h-full w-full flex-col">{children}</div>
    </div>
  );
};

export const LiquidGlassSurface: React.FC<{
  tokens?: LiquidGlassTokens;
  elevated?: boolean;
  onClick?: () => void;
  className?: string;
  style?: React.CSSProperties;
  children: React.ReactNode;
}> = ({
  tokens: overrideTokens,
  elevated = false,
  onClick,
  className = '',
  style = {},
  children,
}) => {
  const app = useBibleApp();
  const tokens = overrideTokens ?? app.tokens;

  const baseBg = elevated ? tokens.glassSurfaceElevated : tokens.glassSurface;
  const overlayGradient = tokens.isCrystalClear
    ? `linear-gradient(135deg, ${cssWithAlpha(
        tokens.specularTop,
        tokens.isDark ? 0.2 : 0.62
      )} 0%, ${
        tokens.isDark ? 'rgba(255,255,255,0.03)' : 'rgba(255,255,255,0.1)'
      } 45%, ${cssWithAlpha(tokens.accentColor, tokens.isDark ? 0.08 : 0.06)} 100%)`
    : tokens.isGlassActive
    ? `linear-gradient(180deg, ${cssWithAlpha(
        tokens.specularTop,
        tokens.isDark ? 0.12 : 0.45
      )} 0%, transparent 60%, ${cssWithAlpha(tokens.accentColor, 0.05)} 100%)`
    : undefined;

  const blurVal = tokens.isGlassActive && tokens.blurIntensityDp > 0.5 ? tokens.blurIntensityDp : 0;

  const combinedStyle: React.CSSProperties = {
    backgroundColor: baseBg,
    backgroundImage: overlayGradient,
    backdropFilter: blurVal > 0 ? `blur(${blurVal}px) saturate(140%)` : undefined,
    WebkitBackdropFilter: blurVal > 0 ? `blur(${blurVal}px) saturate(140%)` : undefined,
    border: `${tokens.isCrystalClear ? '1.15px' : tokens.isGlassActive ? '1px' : '0.7px'} solid ${
      tokens.borderTop
    }`,
    borderBottomColor: tokens.borderBottom,
    boxShadow: tokens.isCrystalClear
      ? `0 10px 28px -6px ${cssWithAlpha(tokens.accentColor, 0.2)}, 0 4px 12px rgba(0,0,0,${
          tokens.isDark ? 0.45 : 0.09
        })`
      : tokens.isGlassActive
      ? `0 8px 24px -6px ${cssWithAlpha(tokens.accentColor, 0.15)}, 0 3px 10px rgba(0,0,0,${
          tokens.isDark ? 0.38 : 0.08
        })`
      : `0 3px 10px rgba(0,0,0,${tokens.isDark ? 0.35 : 0.07})`,
    ...style,
  };

  if (onClick) {
    return (
      <div
        role="button"
        tabIndex={0}
        onClick={onClick}
        onKeyDown={(e) => {
          if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onClick();
          }
        }}
        className={`cursor-pointer transition-transform active:scale-[0.99] ${className}`}
        style={combinedStyle}
      >
        {children}
      </div>
    );
  }

  return (
    <div className={className} style={combinedStyle}>
      {children}
    </div>
  );
};

export const LiquidGlassButton: React.FC<{
  tokens?: LiquidGlassTokens;
  onClick?: () => void;
  disabled?: boolean;
  className?: string;
  children: React.ReactNode;
}> = ({ tokens: overrideTokens, onClick, disabled = false, className = '', children }) => {
  const app = useBibleApp();
  const tokens = overrideTokens ?? app.tokens;

  const radiusClass =
    tokens.buttonStyle === ButtonStyleOption.LIQUID_GLASS ||
    tokens.buttonStyle === ButtonStyleOption.PILL_FILLED
      ? 'rounded-full'
      : tokens.buttonStyle === ButtonStyleOption.OUTLINED_GLASS
      ? 'rounded-2xl'
      : 'rounded-xl';

  const getStyle = (): React.CSSProperties => {
    switch (tokens.buttonStyle) {
      case ButtonStyleOption.OUTLINED_GLASS:
        return {
          backgroundColor: cssWithAlpha(tokens.accentColor, 0.16),
          color: tokens.textPrimary,
          border: `1.2px solid ${cssWithAlpha(tokens.accentColor, 0.78)}`,
        };
      case ButtonStyleOption.LIQUID_GLASS: {
        const containerAlpha = tokens.isCrystalClear ? 0.78 : 0.88;
        const borderCol = tokens.isCrystalClear
          ? tokens.isDark
            ? 'rgba(255,255,255,0.52)'
            : 'rgba(255,255,255,0.85)'
          : 'rgba(255,255,255,0.45)';
        return {
          backgroundColor: cssWithAlpha(tokens.accentColor, containerAlpha),
          color: tokens.onAccentColor,
          border: `${tokens.isCrystalClear ? '1.1px' : '0.9px'} solid ${borderCol}`,
          boxShadow: `0 6px 16px -4px ${cssWithAlpha(tokens.accentColor, 0.35)}`,
        };
      }
      case ButtonStyleOption.PILL_FILLED:
      case ButtonStyleOption.CLASSIC_ELEVATED:
        return {
          backgroundColor: tokens.accentColor,
          color: tokens.onAccentColor,
          boxShadow: '0 3px 10px rgba(0,0,0,0.16)',
        };
    }
  };

  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className={`inline-flex min-h-[44px] items-center justify-center gap-2 px-4 py-2 font-semibold transition-all hover:brightness-105 active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-45 ${radiusClass} ${className}`}
      style={getStyle()}
    >
      {children}
    </button>
  );
};

export const LiquidGlassDialog: React.FC<{
  open: boolean;
  onClose: () => void;
  tokens?: LiquidGlassTokens;
  maxWidthClass?: string;
  children: React.ReactNode;
}> = ({ open, onClose, tokens: overrideTokens, maxWidthClass = 'max-w-lg', children }) => {
  const app = useBibleApp();
  const tokens = overrideTokens ?? app.tokens;

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      style={{
        backgroundColor: `rgba(0,0,0,${tokens.isCrystalClear ? 0.32 : 0.45})`,
        backdropFilter: 'blur(4px)',
      }}
      onClick={onClose}
    >
      <div
        className={`animate-modal-pop w-full ${maxWidthClass}`}
        onClick={(e) => e.stopPropagation()}
      >
        <LiquidGlassSurface tokens={tokens} elevated className="rounded-3xl p-5">
          {children}
        </LiquidGlassSurface>
      </div>
    </div>
  );
};

export interface NavItemSpec {
  route: AppRoute;
  label: string;
  icon: React.ReactNode;
}

export const LiquidGlassBottomNav: React.FC<{
  items: NavItemSpec[];
  currentRoute: AppRoute;
  onNavigate: (route: AppRoute) => void;
}> = ({ items, currentRoute, onNavigate }) => {
  const { tokens } = useBibleApp();

  if (tokens.navBarAppearance === NavBarAppearance.FLOATING_GLASS_DOCK) {
    return (
      <div className="pointer-events-none fixed inset-x-0 bottom-0 z-30 flex justify-center px-3 pb-3 pt-1">
        <LiquidGlassSurface
          tokens={tokens}
          elevated
          className="pointer-events-auto w-full max-w-[680px] rounded-[28px] p-1.5"
        >
          <nav className="flex items-center justify-around gap-1">
            {items.map((item) => {
              const selected = currentRoute === item.route;
              return (
                <button
                  key={item.route}
                  type="button"
                  onClick={() => onNavigate(item.route)}
                  className="flex min-h-[48px] flex-1 flex-col items-center justify-center rounded-[22px] px-1 py-1.5 transition-all"
                  style={{
                    backgroundColor: selected
                      ? cssWithAlpha(tokens.accentColor, tokens.isDark ? 0.28 : 0.18)
                      : 'transparent',
                    color: selected ? tokens.accentColor : tokens.textSecondary,
                  }}
                >
                  {item.icon}
                  <span
                    className={`mt-0.5 truncate text-[10.5px] ${
                      selected ? 'font-bold' : 'font-medium'
                    }`}
                  >
                    {item.label}
                  </span>
                </button>
              );
            })}
          </nav>
        </LiquidGlassSurface>
      </div>
    );
  }

  return (
    <div
      className="fixed inset-x-0 bottom-0 z-30 border-t"
      style={{
        backgroundColor: tokens.glassSurfaceElevated,
        borderColor: tokens.borderTop,
        backdropFilter:
          tokens.navBarAppearance === NavBarAppearance.FROSTED_FULL_BAR &&
          tokens.blurIntensityDp > 0.5
            ? `blur(${tokens.blurIntensityDp}px)`
            : undefined,
      }}
    >
      <nav className="mx-auto flex max-w-[680px] items-center justify-around px-2 py-1.5">
        {items.map((item) => {
          const selected = currentRoute === item.route;
          return (
            <button
              key={item.route}
              type="button"
              onClick={() => onNavigate(item.route)}
              className="flex min-h-[48px] flex-1 flex-col items-center justify-center rounded-xl py-1 transition-colors"
              style={{
                color: selected ? tokens.accentColor : tokens.textSecondary,
              }}
            >
              <div
                className="flex items-center justify-center rounded-full px-3 py-0.5"
                style={{
                  backgroundColor: selected
                    ? cssWithAlpha(tokens.accentColor, 0.18)
                    : 'transparent',
                }}
              >
                {item.icon}
              </div>
              <span
                className={`mt-0.5 truncate text-[11px] ${
                  selected ? 'font-bold' : 'font-medium'
                }`}
              >
                {item.label}
              </span>
            </button>
          );
        })}
      </nav>
    </div>
  );
};

export const GlassSwatchCircle: React.FC<{
  argb: number;
  selected: boolean;
  title: string;
  onClick: () => void;
}> = ({ argb, selected, title, onClick }) => {
  return (
    <button
      type="button"
      title={title}
      onClick={onClick}
      className="flex h-11 w-11 shrink-0 items-center justify-center rounded-full transition-transform active:scale-95"
    >
      <span
        className="h-9 w-9 rounded-full transition-all"
        style={{
          backgroundColor: argbToCss(argb, 1),
          border: selected ? '3px solid #FFFFFF' : '1.5px solid rgba(128,128,128,0.45)',
          boxShadow: selected ? '0 0 0 2px rgba(59,130,246,0.75)' : undefined,
        }}
      />
    </button>
  );
};

export const ToastBanner: React.FC = () => {
  const { toastMessage, tokens } = useBibleApp();
  if (!toastMessage) return null;

  return (
    <div className="pointer-events-none fixed inset-x-0 bottom-24 z-50 flex justify-center px-4">
      <div
        className="animate-modal-pop rounded-full px-4 py-2 text-xs font-semibold shadow-lg"
        style={{
          backgroundColor: tokens.accentColor,
          color: tokens.onAccentColor,
        }}
      >
        {toastMessage}
      </div>
    </div>
  );
};
