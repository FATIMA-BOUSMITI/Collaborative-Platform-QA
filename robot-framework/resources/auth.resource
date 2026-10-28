*** Settings ***
Library    Collections
Library    RequestsLibrary
Variables    ../variables/env.py

*** Keywords ***
Require API configuration
    ${base_url}=    Set Variable    ${QA_API_BASE_URL}
    ${email}=    Set Variable    ${QA_API_EMAIL}
    ${password}=    Set Variable    ${QA_API_PASSWORD}
    Should Not Be Empty    ${base_url}    QA_API_BASE_URL is required.
    Should Not Be Empty    ${email}    QA_API_EMAIL is required.
    Should Not Be Empty    ${password}    QA_API_PASSWORD is required.
    Set Suite Variable    ${API_BASE_URL}    ${base_url}
    Set Suite Variable    ${API_EMAIL}    ${email}
    Set Suite Variable    ${API_PASSWORD}    ${password}

Create API session
    Create Session    api    ${API_BASE_URL}

Authenticate QA account
    ${payload}=    Create Dictionary    email=${API_EMAIL}    password=${API_PASSWORD}
    ${response}=    POST On Session    api    /api/auth/login    json=${payload}    expected_status=200
    RETURN    ${response}
