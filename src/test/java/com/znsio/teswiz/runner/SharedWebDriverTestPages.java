package com.znsio.teswiz.runner;

import java.nio.file.Files;
import java.nio.file.Path;

final class SharedWebDriverTestPages {
    private SharedWebDriverTestPages() {
    }

    static Path writeTestPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Playwright Bridge</title>
                </head>
                <body>
                  <h1 id="title">Playwright Bridge</h1>
                  <input id="name" />
                  <button id="save" onclick="document.getElementById('status').innerText = 'Saved ' + document.getElementById('name').value;">Save</button>
                  <div id="status">Idle</div>
                  <ul>
                    <li class="item">one</li>
                    <li class="item">two</li>
                  </ul>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("shared-webdriver-bridge-", ".html");
        Files.writeString(file, html);
        return file;
    }

    static Path writeFrameTestPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Playwright Frame Bridge</title>
                </head>
                <body>
                  <div id="outside">Outside Frame</div>
                  <iframe id="details-frame" name="details-frame"
                    srcdoc="<html><body><div id='inside'>Inside Frame</div></body></html>"></iframe>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("shared-webdriver-frame-", ".html");
        Files.writeString(file, html);
        return file;
    }

    static Path writeAlertTestPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Playwright Alert Bridge</title>
                </head>
                <body>
                  <button id="alertButton" onclick="setTimeout(() => { alert('Plain alert'); document.getElementById('result').innerText='alert-done'; }, 0);">Alert</button>
                  <button id="promptButton" onclick="setTimeout(() => { const value = prompt('Enter your name', 'Guest'); document.getElementById('result').innerText='prompt:' + value; }, 0);">Prompt</button>
                  <div id="result">idle</div>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("shared-webdriver-alert-", ".html");
        Files.writeString(file, html);
        return file;
    }

    static Path writeShadowDomTestPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Playwright Shadow Bridge</title>
                </head>
                <body>
                  <div id="shadow-host"></div>
                  <script>
                    const host = document.getElementById('shadow-host');
                    const root = host.attachShadow({ mode: 'open' });
                    root.innerHTML = `
                      <div id="shadow-text">Inside Shadow Root</div>
                      <button data-testid="shadow-button" onclick="this.getRootNode().getElementById('shadow-status').textContent = 'clicked'">Press</button>
                      <div id="shadow-status">idle</div>
                      <span class="shadow-item">one</span>
                      <span class="shadow-item">two</span>
                    `;
                  </script>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("shared-webdriver-shadow-", ".html");
        Files.writeString(file, html);
        return file;
    }

    static Path writeDelayedElementPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Playwright Timeout Bridge</title>
                  <script>
                    window.setTimeout(() => {
                      const element = document.createElement('div');
                      element.id = 'delayed';
                      element.innerText = 'Ready Later';
                      document.body.appendChild(element);
                    }, 300);
                  </script>
                </head>
                <body>
                  <h1 id="title">Timeout Bridge</h1>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("shared-webdriver-timeout-", ".html");
        Files.writeString(file, html);
        return file;
    }

    static Path writeConsoleLogPage() throws Exception {
        String html = """
                <!doctype html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <title>Playwright Log Bridge</title>
                  <script>
                    console.log('playwright-browser-log');
                    console.warn('playwright-browser-warning');
                  </script>
                </head>
                <body>
                  <h1 id="title">Log Bridge</h1>
                </body>
                </html>
                """;
        Path file = Files.createTempFile("shared-webdriver-log-", ".html");
        Files.writeString(file, html);
        return file;
    }
}
