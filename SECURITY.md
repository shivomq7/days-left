# Security Policy for Days Left

## Overview
Days Left implements multiple security layers to protect user data and ensure safe operation.

## Security Features

### 1. **Encrypted Storage**
- All sensitive data (API keys, user settings) are stored using Android's `EncryptedSharedPreferences`
- AES-256-GCM encryption at rest
- Hardware-backed keystore when available

### 2. **Input Validation**
- All user inputs are validated and sanitized
- Prevents SQL injection attacks
- Date, number, and text validation rules
- Maximum string length enforcement

### 3. **Network Security**
- HTTPS enforcement for all network calls
- Certificate pinning to prevent MITM attacks
- Network security configuration in `network_security_config.xml`
- No cleartext traffic allowed

### 4. **Code Obfuscation**
- ProGuard/R8 enabled for release builds
- Method and class name obfuscation
- Logging stripped from production builds
- Source file information retained for crash reporting

### 5. **Manifest Security Hardening**
- `android:allowBackup="false"` - Prevents unauthorized backup extraction
- `android:usesCleartextTraffic="false"` - Blocks unencrypted connections
- Minimal permission declarations
- Activities not exported unless necessary

### 6. **API Key Management**
- Gemini API keys never hardcoded
- Loaded from `.env` file (not committed to repository)
- Validated before storage
- Can be cleared on logout

## Best Practices for Developers

### When Adding New Features:
1. **Always validate input** using `InputValidator`
2. **Use `SecureKeyManager`** for any sensitive data
3. **Implement network calls** with `NetworkSecurityManager`
4. **Never log sensitive data** - Timber logs are stripped in production
5. **Use HTTPS only** - Enforce via network security config

### Dependency Management:
- Keep dependencies updated regularly
- Review security advisories for all libraries
- Use version pinning in `build.gradle.kts`

## Reporting Security Issues

**Do NOT open public issues for security vulnerabilities.**

Please report security issues to: [Your contact method]

Include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact
- Suggested fix (if available)

## Data Privacy

- All user data stored locally on device
- Gemini API calls may be subject to Google's privacy policy
- No analytics or tracking enabled
- User can delete all data via app settings

## Compliance

- Target API Level 34+ (latest Android security standards)
- OWASP Mobile Security standards compliance
- Regular security reviews recommended

## Version History

- **v1.0** (2026-07-02) - Initial security hardening
  - Added EncryptedSharedPreferences
  - Implemented input validation
  - Added network security config
  - Enabled ProGuard obfuscation
