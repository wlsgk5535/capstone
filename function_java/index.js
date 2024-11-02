/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {onRequest} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");

// Create and deploy your first functions
// https://firebase.google.com/docs/functions/get-started

exports.helloWorld = onRequest((request, response) => {
  logger.info("Hello logs!", {structuredData: true});
  response.send("Hello from Firebase!");
});

// const {Storage} = require("@google-cloud/storage");
const functions = require("firebase-functions");
const puppeteer = require("puppeteer");
const admin = require("firebase-admin");
const chromium = require("@sparticuz/chromium");

// Firebase 초기화
admin.initializeApp();
// const storage = new Storage();

/**
 * 이 함수는 Firebase Storage에서 이미지를 다운로드합니다.
 * @param {string} imagePath - Firebase Storage에 있는 이미지 파일의 경로
 * @return {Promise<string>} 로컬에 저장된 이미지 파일의 경로를 반환합니다.
 */
/* async function downloadImageFromStorage(imagePath) {
  const bucket = storage.bucket(
      "fashion-item-system.appspot.com",
  );
  const file = bucket.file(imagePath);
  const localFilePath = `/tmp/${imagePath.split("/").pop()}`; // 임시로 저장할 로컬 경로

  await file.download({destination: localFilePath});
  console.log(`Downloaded ${imagePath} to ${localFilePath}`);
  return localFilePath;
}*/

// 상품 세부 페이지 URL을 생성하는 함수
/**
 * Generates the URL for a product detail page on Musinsa.
 *
 * @param {string} itemId - The unique ID of the product.
 * @return {string} The URL of the product's detail page.
 */
function generateProductUrl(itemId) {
  return `https://www.musinsa.com/products/${itemId}`;
}

// 상품의 카테고리를 확인하는 함수
/**
 * Checks the category of the product from the given product page.
 *
 * @param {object} page - The Puppeteer page instance.
 * @param {string} productUrl - The URL of the product page to check.
 * @return {string|null}
 *  - Returns the category name if found, or null if not found.
 */
async function checkCategory(page, productUrl) {
  try {
    await page.goto(productUrl, {waitUntil: "networkidle2"});

    // 페이지 소스 로드 후 data-category-name 속성 확인
    const category = await page.$eval(
        "a[data-category-name]",
        (element) => element.getAttribute("data-category-name"),
    );

    if (category) {
      console.log(`Product URL: ${productUrl} - Category Found: ${category}`);
      return category;
    } else {
      console.log(`Product URL: ${productUrl} - No Category Found`);
      return null;
    }
  } catch (error) {
    console.error(
        `Error fetching product page ${productUrl}: ${error.message}`,
    );
    return null;
  }
}


// 페이지에서 상품 목록을 처리하는 함수
/**
 * Processes the product list on the page
 *  and checks the category of each product.
 *
 * @param {object} browser - The Puppeteer browser instance.
 * @param {string} pageUrl - The URL of the Musinsa page to process.
 * @param {string} imageFilename
 *  - The name of the image file related to the page.
 */
async function processPage(browser, pageUrl, imageFilename) {
  const page = await browser.newPage();

  try {
    await page.goto(pageUrl, {waitUntil: "networkidle2"});

    // 상품 ID 추출
    const itemIds = await page.$$eval(
        "div[data-item-id]",
        (elements) => elements.map((el) => el.getAttribute("data-item-id")),
    );

    if (itemIds.length > 0) {
      for (const itemId of itemIds) {
        const productUrl = generateProductUrl(itemId);
        const category = await checkCategory(page, productUrl);

        // 카테고리 확인 후 결과 출력
        if (
          category &&
          (category === "패션소품" || category === "신발" || category === "가방")
        ) {
          console.log(
              `패션소품 또는 신발을 발견했습니다! (ID: ${itemId}, Category: ${category})`,
          );
        } else {
          console.log(`찾는 카테고리 없음 (ID: ${itemId}, Category: ${category})`);
        }
      }
    } else {
      console.log("상품 ID를 찾을 수 없습니다.");
    }
  } catch (error) {
    console.error(`Error processing page ${pageUrl}: ${error.message}`);
  } finally {
    await page.close();
  }
}s

// Firebase Function에서 실행할 메인 함수
exports.checkProductCategory = functions.https.onRequest(async (req, res) => {
  const browser = await puppeteer.launch({
    // executablePath:
    //  "C:/Users/AHJ/.cache/puppeteer/chrome/win64-130.0.6723.58/" +
    //  "chrome-win64/chrome.exe",
    executablePath: await chromium.executablePath(),
    headless: true,
    args: ["--no-sandbox", "--disable-setuid-sandbox"],
    // Puppeteer에서 사용할 Chrome 경로를 명시
  });
  // Firebase Storage에서 이미지 경로를 설정 (고정된 예시 경로 사용)
  const imageFilename = "images_Men/businesscasual/10000.jpg"; // 고정된 예시 파일 경로

  // 이미지 파일 다운로드
  // const localFilePath = await downloadImageFromStorage(imageFilename);

  // Musinsa 페이지 URL 생성
  const imageNumber = imageFilename.match(/\d+/)[0]; // 이미지에서 숫자 추출
  const pageUrl = `https://www.musinsa.com/app/styles/views/${imageNumber}`;

  await processPage(browser, pageUrl, imageFilename);

  await browser.close();

  res.send("상품 카테고리 체크 완료");
});
