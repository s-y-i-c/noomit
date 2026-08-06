import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import "../features/theme/themes/light.css";
import "../features/theme/themes/dark.css";
import { StoreProvider } from "@/features/store/StoreProvider";
import { ThemeSync } from "@/features/theme/store/ThemeSync";
import React from "react";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "Noomit",
  description: "Noomit service",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="ko"
      data-theme="light"
      className={`${geistSans.variable} ${geistMono.variable}`}
    >
      <body>
        <StoreProvider>
          <ThemeSync />
          {children}
        </StoreProvider>
      </body>
    </html>
  );
}
