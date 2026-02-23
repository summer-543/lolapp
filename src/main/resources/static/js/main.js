let allChampions = [];
// スプラッシュアート表示用キャラクターID
let currentModalChampId = null;

function escapeHTML(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

const activeFilters = {
    roles: new Set(),
    lanes: new Set(),
    regions: new Set(),
    races: new Set()
};

let currentQuestionIndex = 0;
let answerHistory = [];

const JpMap = {
    roles: { 'Fighter': 'ファイター', 'Tank': 'タンク', 'Mage': 'メイジ', 'Assassin': 'アサシン', 'Marksman': 'マークスマン', 'Support': 'サポート' },
    lanes: { 'Top': 'トップ', 'Jungle': 'ジャングル', 'Mid': 'ミッド', 'Bot': 'ボット', 'Support': 'サポート' },
    regions: {
        'Demacia': 'デマーシア', 'Noxus': 'ノクサス', 'Ionia': 'アイオニア', 'Piltover': 'ピルトーヴァー',
        'Zaun': 'ゾウン', 'Freljord': 'フレヨルド', 'Bilgewater': 'ビルジウォーター', 'Shurima': 'シュリーマ',
        'Shadow Isles': 'シャドウアイル', 'Targon': 'ターゴン', 'Ixtal': 'イシュタル', 'The Void': 'ヴォイド',
        'Bandle City': 'バンドルシティ', 'Runeterra': 'ルーンテラ'
    },
    races: {
        'Human': '人間', 'Yordle': 'ヨードル', 'Vastaya': 'ヴァスタヤ', 'Voidborn': 'ヴォイドボーン',
        'Ascended': '超越者', 'Darkin': 'ダーキン', 'Demon': '悪魔', 'Spirit': '精霊',
        'Undead': 'アンデッド', 'Golem/Robot': '自動人形', 'Cyborg': 'サイボーグ',
        'Dragon': 'ドラゴン', 'Celestial/Aspect': '星霊/天界', 'Troll': 'トロール',
        'Minotaur': 'ミノタウロス', 'Brackern': 'ブラッカン', 'Animal/Beast': '獣・動物',
        'Mutant/Monster': '怪物'
    }
};

const testQuestions = [
    {
        text: "戦場ではどんな役割を担いたい？",
        options: [
            { label: "前線で攻防の要になる", type: "roles", value: "Fighter" },
            { label: "強固な盾となり味方を守る", type: "roles", value: "Tank" },
            { label: "強力な魔法で敵を粉砕する", type: "roles", value: "Mage" },
            { label: "隙を突き一瞬で暗殺する", type: "roles", value: "Assassin" },
            { label: "遠距離から絶え間なく攻撃する", type: "roles", value: "Marksman" },
            { label: "味方を回復や強化で支える", type: "roles", value: "Support" },
            { label: "こだわらない", type: "any", value: "Any" }
        ]
    },
    {
        text: "戦場での好きな立ち回りは？",
        options: [
            { label: "1対1で孤高の戦いを楽しむ", type: "lanes", value: "Top" },
            { label: "森に潜み、奇襲で味方を助ける", type: "lanes", value: "Jungle" },
            { label: "マップの中央で試合の展開を動かす", type: "lanes", value: "Mid" },
            { label: "相方と息を合わせて2人で戦い抜く", type: "lanes", value: "Bot" },
            { label: "視野を広く保ち、チームを勝利に導く", type: "lanes", value: "Support" },
            { label: "どこでもいい・わからない", type: "any", value: "Any" }
        ]
    },
    {
        text: "敵との距離感は？",
        options: [
            { label: "敵の懐に飛び込んで戦う", type: "rangeType", value: "Melee" },
            { label: "安全な距離から狙い撃つ", type: "rangeType", value: "Ranged" },
            { label: "どちらでもいい", type: "any", value: "Any" }
        ]
    },
    {
        text: "主な攻撃手段は？",
        options: [
            { label: "武器による「物理的な攻撃」", type: "damagePrimary", value: "Physical" },
            { label: "魔法や超能力による「魔法攻撃」", type: "damagePrimary", value: "Magic" },
            { label: "物理と魔法を両方使いこなす", type: "damagePrimary", value: "Mixed" },
            { label: "こだわらない", type: "any", value: "Any" }
        ]
    },
    {
        text: "操作難易度の希望は？",
        options: [
            { label: "シンプルで使いやすい", type: "difficulty", value: 1 },
            { label: "少し練習が必要だが応用が利く", type: "difficulty", value: 2 },
            { label: "非常に難しいがポテンシャルが高い", type: "difficulty", value: 3 },
            { label: "気にしない", type: "any", value: "Any" }
        ]
    },
    {
        text: "キャラクターの性別は？",
        options: [
            { label: "男性", type: "visuals", value: "Male" },
            { label: "女性", type: "visuals", value: "Female" },
            { label: "性別なし・人外", type: "visuals", value: "Genderless" },
            { label: "こだわらない", type: "any", value: "Any" }
        ]
    },
    {
        text: "種族や形態の好みは？",
        options: [
            { label: "人間", type: "visuals", value: "Human" },
            { label: "小さくて可愛いマスコット", type: "visuals", value: "Yordle" },
            { label: "動物の特徴を持つ獣人", type: "visuals", value: "Vastaya" },
            { label: "神、精霊、超越者", type: "visuals_multi", value: ["Ascended", "Spirit", "Celestial/Aspect", "God/Entity"] },
            { label: "怪物、悪魔、アンデッド", type: "visuals_multi", value: ["Voidborn", "Darkin", "Demon", "Undead", "Monster", "Mutant/Monster"] },
            { label: "ロボットやサイボーグ", type: "visuals_multi", value: ["Golem/Robot", "Cyborg"] },
            { label: "こだわらない", type: "any", value: "Any" }
        ]
    },
    {
        text: "見た目や雰囲気の好みは？",
        options: [
            { label: "かわいい・癒やし", type: "visuals", value: "Cute" },
            { label: "かっこいい・スタイリッシュ", type: "visuals", value: "Handsome/Cool" },
            { label: "美しい・優雅", type: "visuals", value: "Beautiful/Elegant" },
            { label: "力強い・ワイルド", type: "visuals_multi", value: ["Tough/Macho", "Wild"] },
            { label: "ダーク・ホラー", type: "visuals_multi", value: ["Edgy/Dark", "Creepy/Horror"] },
            { label: "威厳がある・神々しい", type: "visuals", value: "Majestic" },
            { label: "こだわらない", type: "any", value: "Any" }
        ]
    }
];

window.onload = async () => {
    try {
        const response = await fetch('/api/champions');
        if (!response.ok) throw new Error('Network response was not ok');
        allChampions = await response.json();
        renderChampionGrid(allChampions);
        setupCustomDropdowns();
    } catch (error) {
        console.error("データの取得に失敗しました", error);
        document.getElementById('champion-grid').innerHTML =
            '<div class="col-span-full text-center text-red-500 py-10 font-bold lg:text-lg">データの読み込みに失敗しました。サーバーが起動しているか確認してください。</div>';
    }
};

function switchView(viewId) {
    const views = ['home-view', 'champions-view', 'test-view', 'test-result-view', 'quiz-setup-view', 'quiz-view', 'quiz-result-view'];

    document.getElementById(viewId).style.zIndex = "20";

    views.forEach(id => {
        const el = document.getElementById(id);
        if (id === viewId) {
            el.classList.remove('hidden');
            setTimeout(() => el.classList.remove('opacity-0'), 10);
        } else {
            el.style.zIndex = "10";
            el.classList.add('opacity-0');
            setTimeout(() => el.classList.add('hidden'), 300);
        }
    });
}

// =====================================
// 適正診断テスト
// =====================================
function startTest() {
    currentQuestionIndex = 0;
    answerHistory = [];
    switchView('test-view');
    renderQuestion();
}

function renderQuestion() {
    const q = testQuestions[currentQuestionIndex];
    document.getElementById('question-progress').textContent = `QUESTION ${currentQuestionIndex + 1} / ${testQuestions.length}`;
    document.getElementById('question-text').textContent = q.text;

    const container = document.getElementById('options-container');
    container.innerHTML = '';

    q.options.forEach(opt => {
        const btn = document.createElement('button');
        btn.className = 'bg-[#1e2328] border-2 border-gray-600 hover:border-gold hover:bg-[#c8aa6e] hover:bg-opacity-20 text-gray-200 font-medium text-sm sm:text-base lg:text-lg xl:text-xl py-3 px-6 lg:py-4 lg:px-8 rounded-lg lg:rounded-xl transition-all duration-200 shadow text-left';
        btn.textContent = opt.label;
        btn.onclick = () => selectOption(opt);
        container.appendChild(btn);
    });

    const backBtn = document.getElementById('btn-back-question');
    if (currentQuestionIndex > 0) {
        backBtn.classList.remove('hidden');
    } else {
        backBtn.classList.add('hidden');
    }
}

function goBackQuestion() {
    if (currentQuestionIndex > 0) {
        answerHistory.pop();
        currentQuestionIndex--;
        const testView = document.getElementById('test-view');
        testView.classList.remove('animate-fade-in');
        void testView.offsetWidth;
        testView.classList.add('animate-fade-in');
        renderQuestion();
    }
}

function selectOption(opt) {
    answerHistory.push(opt);
    currentQuestionIndex++;

    if (currentQuestionIndex < testQuestions.length) {
        const testView = document.getElementById('test-view');
        testView.classList.remove('animate-fade-in');
        void testView.offsetWidth;
        testView.classList.add('animate-fade-in');
        renderQuestion();
    } else {
        showResult();
    }
}

async function showResult() {
    switchView('test-result-view');
    const container = document.getElementById('result-champions-container');
    container.innerHTML = '<div class="text-gold text-xl font-bold animate-pulse py-10 w-full text-center tracking-widest">データを解析中...</div>';

    try {
        const payload = {
            answers: answerHistory.map(opt => {
                let values = [];
                if (opt.type !== "any") {
                    if (Array.isArray(opt.value)) {
                        values = opt.value.map(String);
                    } else {
                        values = [String(opt.value)];
                    }
                }
                return { type: opt.type, values };
            })
        };

        const response = await fetch('/api/aptitude-test', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error('API request failed');

        const top3 = await response.json();

        container.innerHTML = '';
        if (top3[0]) container.innerHTML += renderResultCard(top3[0], true, 'order-1 md:order-2');
        if (top3[1]) container.innerHTML += renderResultCard(top3[1], false, 'order-2 md:order-1');
        if (top3[2]) container.innerHTML += renderResultCard(top3[2], false, 'order-3 md:order-3');

    } catch (error) {
        console.error("Error calculating test score:", error);
        container.innerHTML = '<div class="text-red-400 py-10 w-full text-center font-bold">通信エラーが発生しました。もう一度お試しください。</div>';
    }
}

function renderResultCard(champ, isFirst, orderClass) {
    const sizeClass = isFirst ? "w-full max-w-[220px] md:max-w-[280px] lg:max-w-[340px] xl:max-w-[360px] scale-100 z-10" : "w-full max-w-[180px] md:max-w-[240px] lg:max-w-[280px] xl:max-w-[300px] md:scale-95 opacity-90 hover:opacity-100";
    const shadowClass = isFirst ? "shadow-[0_0_30px_rgba(200,170,110,0.4)]" : "shadow-lg";
    const borderClass = isFirst ? "border-gold" : "border-gray-500 hover:border-gold";

    let nameSizeClass = "text-2xl sm:text-3xl lg:text-4xl";
    if (champ.name.length >= 11) {
        nameSizeClass = "text-sm sm:text-base lg:text-lg";
    } else if (champ.name.length >= 8) {
        nameSizeClass = "text-base sm:text-lg lg:text-xl";
    } else if (champ.name.length >= 6) {
        nameSizeClass = "text-xl sm:text-2xl lg:text-3xl";
    }

    return `
        <div class="flex flex-col items-center ${sizeClass} ${orderClass} transition-all duration-300 mx-1 md:mx-2 mt-6">
            <div class="w-full bg-panel border-2 ${borderClass} rounded-xl overflow-hidden ${shadowClass} flex flex-col">
                
                <div class="relative pt-[100%] bg-black cursor-pointer overflow-hidden group" onclick="openSplashFromResult(${champ.id})">
                    <img src="${champ.tilePath || champ.imagePath}" onerror="this.onerror=null; this.src='${champ.imagePath}'" class="absolute top-0 left-0 w-full h-full object-cover object-center opacity-90 group-hover:opacity-100 group-hover:scale-105 transition-all duration-500">
                    
                    <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-30 transition-all flex items-center justify-center">
                        <svg class="w-10 h-10 lg:w-16 lg:h-16 text-white opacity-0 group-hover:opacity-100 transition-opacity drop-shadow-lg" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path></svg>
                    </div>
                    
                    ${isFirst ? '<div class="absolute top-0 right-0 bg-gold text-black text-xs lg:text-sm xl:text-base font-black px-3 py-1.5 lg:py-2 rounded-bl-lg">BEST MATCH</div>' : ''}
                </div>
                
                <div class="p-4 lg:p-6 flex flex-col items-center bg-[#1e2328] relative z-10 border-t border-gray-700/50">
                    <p class="lol-title text-gold text-[10px] sm:text-xs lg:text-sm tracking-[0.15em] font-bold mb-1.5 truncate w-full text-center">${escapeHTML(champ.title)}</p>
                    
                    <h3 class="lol-title ${nameSizeClass} font-black text-white tracking-tight truncate w-full text-center mb-4 lg:mb-6">${escapeHTML(champ.name)}</h3>
                    
                    <button onclick="openModal(${champ.id})" class="w-full bg-[#0a1428] hover:bg-[#c8aa6e] text-gray-300 hover:text-white border border-gray-600 hover:border-[#c8aa6e] text-[11px] sm:text-xs lg:text-base font-bold py-2.5 lg:py-3.5 px-4 rounded transition-colors">
                        詳細を見る
                    </button>
                </div>
            </div>
        </div>
    `;
}

// =====================================
// LoL知識クイズ
// =====================================
let currentQuizSessionId = "";
let selectedGenre = "ALL";
let selectedDifficulty = "NORMAL";

function selectQuizSetting(type, val) {
    if (type === 'genre') {
        selectedGenre = val;
        ['ALL', 'VISUAL', 'SPELL', 'KNOWLEDGE'].forEach(g => {
            const btn = document.getElementById('btn-genre-' + g);
            btn.className = (g === val) ? "quiz-genre-btn border-2 border-gold bg-gold/20 text-white font-bold py-3 lg:py-4 rounded-lg lg:rounded-xl transition-colors lg:text-lg"
                : "quiz-genre-btn border-2 border-gray-700 bg-[#1e2328] hover:border-gold/50 text-gray-300 font-bold py-3 lg:py-4 rounded-lg lg:rounded-xl transition-colors lg:text-lg";
        });
    } else {
        selectedDifficulty = val;
        ['NORMAL', 'HARD'].forEach(d => {
            const btn = document.getElementById('btn-diff-' + d);
            if (d === val) {
                const activeClass = (d === 'NORMAL') ? "border-blue-500 bg-blue-500/20 text-white" : "border-red-500 bg-red-500/20 text-white";
                btn.className = `quiz-diff-btn border-2 ${activeClass} font-bold py-3 lg:py-4 rounded-lg lg:rounded-xl transition-colors lg:text-lg`;
            } else {
                const hoverBorder = (d === 'NORMAL') ? "hover:border-blue-500/50" : "hover:border-red-500/50";
                btn.className = `quiz-diff-btn border-2 border-gray-700 bg-[#1e2328] ${hoverBorder} text-gray-300 font-bold py-3 lg:py-4 rounded-lg lg:rounded-xl transition-colors lg:text-lg`;
            }
        });
    }
}

async function startQuizSession() {
    switchView('quiz-view');
    document.getElementById('quiz-header-score').textContent = "0";
    document.getElementById('quiz-header-streak').textContent = "0";
    document.getElementById('quiz-header-lives').textContent = selectedDifficulty === 'HARD' ? '❤️' : '❤️❤️❤️';
    document.getElementById('quiz-question-text').textContent = "";
    document.getElementById('quiz-options').innerHTML = '<div class="col-span-full text-gold text-xl lg:text-2xl font-bold animate-pulse py-10 w-full text-center tracking-widest">サーバー接続中...</div>';
    document.getElementById('quiz-image-container').classList.add('hidden');
    document.getElementById('giveup-container').classList.add('hidden');

    try {
        const response = await fetch('/api/quiz/start', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ genre: selectedGenre, difficulty: selectedDifficulty })
        });
        if (!response.ok) throw new Error('Failed to start session');

        const data = await response.json();
        currentQuizSessionId = data.sessionId;

        await fetchNextQuestion();
    } catch (error) {
        console.error(error);
        document.getElementById('quiz-options').innerHTML = '<div class="col-span-full text-red-400 text-center py-10 lg:text-xl">通信エラーが発生しました。</div>';
    }
}

async function fetchNextQuestion() {
    clearStreakAnimation();

    document.getElementById('quiz-question-text').textContent = "読み込み中...";
    document.getElementById('quiz-options').innerHTML = '';
    document.getElementById('quiz-image-container').classList.add('hidden');
    document.getElementById('giveup-container').classList.add('hidden');

    try {
        const response = await fetch(`/api/quiz/next?sessionId=${currentQuizSessionId}`);
        if (!response.ok) throw new Error('Failed to fetch next question');
        const q = await response.json();
        renderQuizQuestion(q);
    } catch (error) {
        console.error(error);
    }
}

function renderQuizQuestion(q) {
    const questionTitle = document.getElementById('quiz-question-text');
    const imgContainer = document.getElementById('quiz-image-container');
    const imgEl = document.getElementById('quiz-image');

    if (q.imageUrl) {
        if (q.imageUrl.includes('/spell/')) {
            imgContainer.classList.add('hidden');
            questionTitle.innerHTML = `<img src="${q.imageUrl}" class="w-16 h-16 sm:w-20 sm:h-20 lg:w-28 lg:h-28 rounded-lg lg:rounded-xl border border-gray-600 shadow-sm object-cover bg-black shrink-0"><span>${escapeHTML(q.text)}</span>`;
        } else {
            questionTitle.textContent = q.text;
            imgEl.src = q.imageUrl;
            imgContainer.classList.remove('hidden');

            if (q.splash && selectedDifficulty === 'HARD') {
                imgContainer.className = "w-36 h-36 sm:w-48 sm:h-48 lg:w-64 lg:h-64 overflow-hidden rounded-2xl shadow-[0_0_20px_rgba(239,68,68,0.5)] border-[3px] border-red-500 bg-black mb-4 lg:mb-8 flex justify-center items-center mx-auto relative";
                imgEl.className = "absolute w-full h-full object-cover transform scale-[5.0] md:scale-[6.0]";
                const x = Math.floor(Math.random() * 60) + 20;
                const y = Math.floor(Math.random() * 40) + 30;
                imgEl.style.transformOrigin = `${x}% ${y}%`;
                imgEl.style.transform = "";
            } else {
                imgContainer.className = "w-full flex justify-center mb-4 lg:mb-8 px-2 sm:px-0";
                imgEl.className = "rounded-xl lg:rounded-2xl shadow-lg w-full max-w-[300px] sm:max-w-[450px] lg:max-w-[600px] xl:max-w-[700px] aspect-video object-cover border-[3px] border-gray-700";
                imgEl.style.transform = "none";
            }
        }
    } else {
        imgContainer.classList.add('hidden');
        questionTitle.textContent = q.text;
    }

    const optionsContainer = document.getElementById('quiz-options');
    optionsContainer.innerHTML = '';

    const isOnlyImages = q.options.every(opt => opt.imageUrl && !opt.text);

    if (isOnlyImages) {
        optionsContainer.className = "grid grid-cols-2 sm:grid-cols-4 gap-4 sm:gap-6 w-full max-w-2xl lg:max-w-4xl mx-auto";
    } else {
        optionsContainer.className = "grid grid-cols-1 sm:grid-cols-2 gap-3 lg:gap-4 w-full";
    }

    q.options.forEach(opt => {
        const btn = document.createElement('button');
        btn.dataset.optId = opt.optionId;

        if (isOnlyImages) {
            btn.className = 'quiz-option-btn bg-[#1e2328] border-2 lg:border-[3px] border-gray-600 hover:border-gold hover:bg-gold/20 rounded-xl lg:rounded-2xl transition-all duration-200 shadow overflow-hidden aspect-square flex items-center justify-center p-2 lg:p-3 w-full relative';
            btn.innerHTML = `<img src="${opt.imageUrl}" class="w-full h-full object-cover rounded-lg shadow-sm bg-black">`;
        } else {
            btn.className = 'quiz-option-btn bg-[#1e2328] border-2 lg:border-[3px] border-gray-600 hover:border-gold hover:bg-gold/20 text-gray-200 font-bold py-2 px-3 lg:py-4 lg:px-4 rounded-xl transition-all duration-200 shadow flex flex-col items-center justify-center gap-2 lg:gap-3 w-full min-h-[70px] lg:min-h-[100px] text-base lg:text-xl xl:text-2xl';
            let content = '';
            if (opt.imageUrl) {
                const imgSize = opt.text ? 'w-14 h-14 sm:w-16 sm:h-16 lg:w-20 lg:h-20' : 'w-24 h-24 sm:w-32 sm:h-32 lg:w-40 lg:h-40';
                content += `<img src="${opt.imageUrl}" class="${imgSize} rounded-md lg:rounded-xl border border-gray-700 object-cover shrink-0 shadow-sm bg-black">`;
            }
            if (opt.text) {
                content += `<span class="leading-tight text-center break-keep">${escapeHTML(opt.text)}</span>`;
            }
            btn.innerHTML = content;
        }

        btn.onclick = () => submitQuizAnswer(q.questionId, opt.optionId);
        optionsContainer.appendChild(btn);
    });

    document.getElementById('giveup-container').classList.remove('hidden');
}

function giveUpQuiz() {
    const scoreStr = document.getElementById('quiz-header-score').textContent;
    const finalScore = parseInt(scoreStr, 10) || 0;
    showGameOver(finalScore, "");
}

async function submitQuizAnswer(qId, optId) {
    const optionsContainer = document.getElementById('quiz-options');
    const buttons = optionsContainer.querySelectorAll('button');
    buttons.forEach(btn => btn.disabled = true);

    try {
        const payload = { sessionId: currentQuizSessionId, questionId: qId, selectedOptionId: optId };
        const response = await fetch('/api/quiz/check', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            const result = await response.json();

            buttons.forEach(btn => {
                const isSquare = btn.classList.contains('aspect-square');
                const baseLayout = isSquare
                    ? 'quiz-option-btn rounded-xl lg:rounded-2xl transition-all duration-300 shadow overflow-hidden aspect-square flex items-center justify-center p-2 lg:p-3 w-full relative'
                    : 'quiz-option-btn font-bold py-2 px-3 lg:py-4 lg:px-4 rounded-xl transition-all duration-300 shadow flex flex-col items-center justify-center gap-2 lg:gap-3 w-full min-h-[70px] lg:min-h-[100px] text-base lg:text-xl xl:text-2xl';

                if (btn.dataset.optId === result.correctOptionId) {
                    btn.className = `${baseLayout} bg-emerald-600/40 border-[3px] lg:border-[4px] border-emerald-500 text-white`;
                } else if (btn.dataset.optId === optId) {
                    btn.className = `${baseLayout} bg-red-600/40 border-[3px] lg:border-[4px] border-red-500 text-white`;
                } else {
                    btn.className = `${baseLayout} bg-[#1e2328] border-2 lg:border-[3px] border-gray-600 opacity-40`;
                }
            });

            document.getElementById('quiz-header-score').textContent = result.currentScore;
            document.getElementById('quiz-header-streak').textContent = result.currentStreak;

            const maxLives = selectedDifficulty === 'HARD' ? 1 : 3;
            const currentLives = Math.max(0, result.remainingLives);
            document.getElementById('quiz-header-lives').textContent = '❤️'.repeat(currentLives) + '🖤'.repeat(maxLives - currentLives);

            if (result.correct) {
                playStreakAnimation(result.currentStreak);
                const waitTime = result.currentStreak >= 3 ? 1500 : 1000;

                if (result.currentStreak >= 3) {
                    // アニメーション終了後に次の問題を表示
                    setTimeout(() => {
                        clearStreakAnimation();
                        setTimeout(fetchNextQuestion, 300);
                    }, waitTime);
                } else {
                    setTimeout(fetchNextQuestion, waitTime);
                }
            } else {
                if (result.gameOver) {
                    setTimeout(() => showGameOver(result.currentScore, result.explanation), 2000);
                } else {
                    setTimeout(fetchNextQuestion, 1500);
                }
            }
        }
    } catch (error) {
        console.error("Check failed", error);
    }
}

let streakTimeout = null;

// ストリーク演出処理（3連続正解以上で発動）
function playStreakAnimation(streak) {
    let text = "";

    if (streak === 3) { text = "KILLING SPREE"; }
    else if (streak === 4) { text = "RAMPAGE"; }
    else if (streak === 5) { text = "UNSTOPPABLE"; }
    else if (streak === 6) { text = "DOMINATING"; }
    else if (streak === 7) { text = "GODLIKE"; }
    else if (streak >= 8) { text = "LEGENDARY"; }

    if (text) {
        const livesContainer = document.getElementById('quiz-header-lives-container');
        const streakContainer = document.getElementById('streak-text-container');
        const headerTextEl = document.getElementById('header-streak-text');
        const lineEl = document.getElementById('streak-underline');
        const bgEl = document.getElementById('streak-bg');

        if (headerTextEl) headerTextEl.textContent = text;

        if (livesContainer) livesContainer.classList.add('opacity-0');

        setTimeout(() => {
            if (livesContainer) livesContainer.classList.add('hidden');
            if (streakContainer) streakContainer.classList.remove('hidden');

            // 表示順序の設定
            const scoreDiv = document.querySelector('.text-left.flex.flex-col');
            if (scoreDiv) { scoreDiv.style.position = 'relative'; scoreDiv.style.zIndex = '50'; }

            const rightStreakDiv = document.querySelector('.text-right.flex.flex-col');
            if (rightStreakDiv) { rightStreakDiv.style.position = 'relative'; rightStreakDiv.style.zIndex = '10'; }

            if (streakContainer) { streakContainer.style.zIndex = '30'; }

            // アニメーションの初期状態をセット
            const elementsToAnimate = [headerTextEl, lineEl, bgEl];

            elementsToAnimate.forEach(el => {
                if (el) {
                    el.style.transition = 'none';
                    el.style.transform = 'translateX(-80px)';
                    el.style.opacity = '0';
                }
            });

            // リフロー
            if (headerTextEl) void headerTextEl.offsetWidth;

            // 中央へスライドイン
            elementsToAnimate.forEach(el => {
                if (el) {
                    el.style.transition = 'all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                    el.style.transform = 'translateX(0)';
                    el.style.opacity = '1';
                }
            });

        }, 150);
    }
}

function clearStreakAnimation() {
    const livesContainer = document.getElementById('quiz-header-lives-container');
    const streakContainer = document.getElementById('streak-text-container');
    const headerTextEl = document.getElementById('header-streak-text');
    const lineEl = document.getElementById('streak-underline');
    const bgEl = document.getElementById('streak-bg');

    if (streakContainer && !streakContainer.classList.contains('hidden')) {
        // 右側へスライドアウト
        const elementsToAnimate = [headerTextEl, lineEl, bgEl];

        elementsToAnimate.forEach(el => {
            if (el) {
                el.style.transition = 'all 0.3s ease-in';
                el.style.transform = 'translateX(80px)';
                el.style.opacity = '0';
            }
        });

        setTimeout(() => {
            if (streakContainer) streakContainer.classList.add('hidden');
            if (livesContainer) {
                livesContainer.classList.remove('hidden');
                setTimeout(() => livesContainer.classList.remove('opacity-0'), 50);
            }
        }, 300);
    }
}

function showGameOver(finalScore, explanation) {
    switchView('quiz-result-view');
    document.getElementById('quiz-score-text').textContent = finalScore;

    let rank = "IRON";
    if (finalScore >= 300) rank = "CHALLENGER";
    else if (finalScore >= 250) rank = "GRANDMASTER";
    else if (finalScore >= 200) rank = "MASTER";
    else if (finalScore >= 150) rank = "DIAMOND";
    else if (finalScore >= 100) rank = "EMERALD";
    else if (finalScore >= 70) rank = "PLATINUM";
    else if (finalScore >= 50) rank = "GOLD";
    else if (finalScore >= 30) rank = "SILVER";
    else if (finalScore >= 10) rank = "BRONZE";

    const rankColors = {
        "IRON": "text-gray-400",
        "BRONZE": "text-amber-700",
        "SILVER": "text-gray-300",
        "GOLD": "text-yellow-400",
        "PLATINUM": "text-teal-200",
        "EMERALD": "text-emerald-400",
        "DIAMOND": "text-blue-400",
        "MASTER": "text-fuchsia-400",
        "GRANDMASTER": "text-red-500",
        "CHALLENGER": "text-sky-300"
    };

    const rankTextEl = document.getElementById('quiz-rank-text');
    rankTextEl.textContent = rank;
    rankTextEl.className = `lol-title text-[clamp(2rem,6vw,4rem)] font-black drop-shadow-xl tracking-wider ${rankColors[rank]}`;

    const rankImgEl = document.getElementById('quiz-rank-img');
    rankImgEl.src = `/img/ranks/emblem-${rank.toLowerCase()}.png`;
    rankImgEl.classList.remove('hidden');
}


// =====================================
// 図鑑＆フィルター ロジック
// =====================================
function renderChampionGrid(championsToRender) {
    const grid = document.getElementById('champion-grid');
    grid.innerHTML = '';
    if (championsToRender.length === 0) {
        grid.innerHTML = '<div class="col-span-full text-center text-gray-400 py-10 font-bold lg:text-lg">条件に一致するチャンピオンが見つかりません。</div>';
        return;
    }
    championsToRender.forEach(champ => {
        const card = document.createElement('div');
        card.className = 'bg-panel border border-gray-700 rounded-lg overflow-hidden cursor-pointer hover:border-gold hover:scale-[1.03] transition-all duration-200 shadow-lg relative group';
        card.onclick = () => openModal(champ.id);

        card.innerHTML = `
            <div class="relative pt-[100%]">
                <img src="${escapeHTML(champ.imagePath)}" alt="${escapeHTML(champ.name)}" class="absolute top-0 left-0 w-full h-full object-cover">
            </div>
            <div class="p-3 lg:p-4">
                <h3 class="lol-title text-white text-sm sm:text-base lg:text-lg font-bold truncate tracking-wide text-center">${escapeHTML(champ.name)}</h3>
            </div>
        `;
        grid.appendChild(card);
    });
}

function setupCustomDropdowns() {
    const dropdowns = document.querySelectorAll('.custom-dropdown');

    document.addEventListener('click', (e) => {
        dropdowns.forEach(dropdown => {
            if (!dropdown.contains(e.target)) {
                dropdown.querySelector('ul').classList.add('hidden');
            }
        });
    });

    dropdowns.forEach(dropdown => {
        const button = dropdown.querySelector('button');
        const menu = dropdown.querySelector('ul');
        const type = dropdown.dataset.type;

        button.addEventListener('click', () => {
            dropdowns.forEach(d => {
                if (d !== dropdown) d.querySelector('ul').classList.add('hidden');
            });
            menu.classList.toggle('hidden');
        });

        const items = menu.querySelectorAll('li');
        items.forEach(item => {
            item.addEventListener('click', () => {
                const value = item.dataset.value;
                if (value) {
                    activeFilters[`${type}s`].add(value);
                    updateActiveFiltersUI();
                    filterChampions();
                }
                menu.classList.add('hidden');
            });
        });
    });
}

document.getElementById('search-input').addEventListener('input', filterChampions);

function updateActiveFiltersUI() {
    const container = document.getElementById('active-filters-container');
    container.innerHTML = '';

    const colors = {
        roles: 'border-[#c8aa6e] text-[#c8aa6e] hover:bg-[#c8aa6e] hover:text-white',
        lanes: 'border-gray-500 text-gray-300 hover:bg-gray-600 hover:text-white hover:border-gray-600',
        regions: 'border-emerald-600 text-emerald-400 hover:bg-emerald-600 hover:text-white hover:border-emerald-600',
        races: 'border-purple-600 text-purple-400 hover:bg-purple-600 hover:text-white hover:border-purple-600'
    };

    for (const [type, set] of Object.entries(activeFilters)) {
        set.forEach(val => {
            const badge = document.createElement('div');
            badge.className = `flex items-center gap-1.5 border bg-black bg-opacity-50 px-3 py-1 lg:px-4 lg:py-1.5 rounded-full text-xs lg:text-sm font-semibold backdrop-blur-sm cursor-pointer transition-colors ${colors[type]}`;
            const labelText = JpMap[type][val] || val;
            badge.innerHTML = `<span>${labelText}</span><span class="text-sm lg:text-base leading-none mb-0.5">&times;</span>`;

            badge.onclick = () => {
                activeFilters[type].delete(val);
                updateActiveFiltersUI();
                filterChampions();
            };
            container.appendChild(badge);
        });
    }
}

function filterChampions() {
    const searchText = document.getElementById('search-input').value.toLowerCase();

    const filtered = allChampions.filter(champ => {
        const matchName = champ.name.toLowerCase().includes(searchText) || champ.champKey.toLowerCase().includes(searchText);

        const matchRole = activeFilters.roles.size === 0 || Array.from(activeFilters.roles).some(r => champ.roles.includes(r));
        const matchLane = activeFilters.lanes.size === 0 || Array.from(activeFilters.lanes).some(l => champ.lanes && champ.lanes.includes(l));
        const matchRegion = activeFilters.regions.size === 0 || Array.from(activeFilters.regions).some(r => champ.regions && champ.regions.includes(r));
        const matchRace = activeFilters.races.size === 0 || Array.from(activeFilters.races).some(r => champ.visuals && champ.visuals.includes(r));

        return matchName && matchRole && matchLane && matchRegion && matchRace;
    });
    renderChampionGrid(filtered);
}

// ==========================================
// チャンピオン詳細モーダル ＆ スプラッシュアート制御
// ==========================================

function openModal(champId) {
    const champ = allChampions.find(c => c.id === champId);
    if (!champ) {
        console.error("Champion not found:", champId);
        return;
    }

    currentModalChampId = champId;

    const bgImg = document.getElementById('modal-bg-img');
    if (bgImg) bgImg.src = champ.loadingPath || champ.imagePath;

    const titleEl = document.getElementById('modal-title');
    if (titleEl) titleEl.textContent = champ.title;

    const nameEl = document.getElementById('modal-name');
    if (nameEl) {
        nameEl.textContent = champ.name;
        const nameLength = champ.name.length;

        if (nameLength >= 13) {
            nameEl.style.fontSize = 'clamp(1.0rem, 2.0vw, 1.9rem)';
        } else if (nameLength === 12) {
            nameEl.style.fontSize = 'clamp(1.1rem, 2.2vw, 2.1rem)';
        } else if (nameLength === 11) {
            nameEl.style.fontSize = 'clamp(1.2rem, 2.4vw, 2.3rem)';
        } else if (nameLength === 10) {
            nameEl.style.fontSize = 'clamp(1.3rem, 2.6vw, 2.6rem)';
        } else if (nameLength === 9) {
            nameEl.style.fontSize = 'clamp(1.4rem, 2.8vw, 2.9rem)';
        } else if (nameLength === 8) {
            nameEl.style.fontSize = 'clamp(1.5rem, 3.3vw, 3.2rem)';
        } else if (nameLength === 7) {
            nameEl.style.fontSize = 'clamp(1.6rem, 3.8vw, 3.7rem)';
        } else if (nameLength === 6) {
            nameEl.style.fontSize = 'clamp(1.8rem, 4.3vw, 4.3rem)';
        } else if (nameLength === 5) {
            nameEl.style.fontSize = 'clamp(2.0rem, 5.0vw, 5.0rem)';
        } else {
            // 1〜5文字
            nameEl.style.fontSize = 'clamp(2.2rem, 5.5vw, 5.5rem)';
        }
    }

    const line1 = document.getElementById('modal-tags-line1');
    const line2 = document.getElementById('modal-tags-line2');
    if (line1) line1.innerHTML = '';
    if (line2) line2.innerHTML = '';

    if (champ.roles && line1) {
        champ.roles.forEach(role => {
            const roleName = JpMap.roles[role] || role;
            line1.innerHTML += `<span class="bg-[#c8aa6e]/20 text-[#c8aa6e] px-2 py-1 lg:px-3 lg:py-1.5 rounded text-xs lg:text-sm xl:text-base font-bold border border-[#c8aa6e] shadow-sm">${roleName}</span>`;
        });
    }
    if (champ.lanes && line1) {
        champ.lanes.forEach(lane => {
            const laneName = JpMap.lanes[lane] || lane;
            line1.innerHTML += `<span class="bg-gray-600/20 text-gray-300 px-2 py-1 lg:px-3 lg:py-1.5 rounded text-xs lg:text-sm xl:text-base font-bold border border-gray-500 shadow-sm">${laneName}</span>`;
        });
    }
    if (champ.regions && line2) {
        champ.regions.forEach(region => {
            const regionName = JpMap.regions[region] || region;
            line2.innerHTML += `<span class="bg-emerald-600/20 text-emerald-400 px-2 py-1 lg:px-3 lg:py-1.5 rounded text-xs lg:text-sm xl:text-base font-bold border border-emerald-600 shadow-sm">${regionName}</span>`;
        });
    }
    if (champ.visuals && line2) {
        champ.visuals.forEach(visual => {
            if (JpMap.races && JpMap.races[visual]) {
                line2.innerHTML += `<span class="bg-purple-600/20 text-purple-400 px-2 py-1 lg:px-3 lg:py-1.5 rounded text-xs lg:text-sm xl:text-base font-bold border border-purple-600 shadow-sm">${JpMap.races[visual]}</span>`;
            }
        });
    }

    const damageEl = document.getElementById('modal-damage');
    if (damageEl) damageEl.textContent = champ.damagePrimary === 'Physical' ? '物理' : (champ.damagePrimary === 'Magic' ? '魔法' : '混合');

    const rangeEl = document.getElementById('modal-range');
    if (rangeEl) rangeEl.textContent = champ.rangeType === 'Ranged' ? '遠隔' : '近接';

    const resourceEl = document.getElementById('modal-resource');
    if (resourceEl) resourceEl.textContent = champ.resourceType || 'なし';

    const diffEl = document.getElementById('modal-difficulty');
    if (diffEl) {
        if (champ.difficulty === 1) diffEl.innerHTML = '★<span class="text-gray-600">★★</span>';
        else if (champ.difficulty === 2) diffEl.innerHTML = '★★<span class="text-gray-600">★</span>';
        else diffEl.innerHTML = '★★★';
    }

    const skillsContainer = document.getElementById('modal-skills-buttons');
    if (skillsContainer) {
        skillsContainer.innerHTML = '';
        if (champ.spells && champ.spells.length > 0) {
            champ.spells.forEach((spell, index) => {
                const btn = document.createElement('button');
                const baseClass = "w-14 h-14 sm:w-16 sm:h-16 lg:w-20 lg:h-20 xl:w-24 xl:h-24 rounded-md border-[3px] overflow-hidden shrink-0 transition-all p-0.5 bg-black";
                btn.className = `${baseClass} ${index === 0 ? 'border-gold scale-110 shadow-[0_0_10px_rgba(200,170,110,0.6)]' : 'border-gray-700 opacity-60 hover:opacity-100 hover:border-gray-400'}`;
                btn.innerHTML = `<img src="${spell.imagePath}" class="w-full h-full object-cover rounded-sm">`;

                btn.onclick = () => {
                    Array.from(skillsContainer.children).forEach(b => {
                        b.className = `${baseClass} border-gray-700 opacity-60 hover:opacity-100 hover:border-gray-400`;
                    });
                    btn.className = `${baseClass} border-gold scale-110 shadow-[0_0_10px_rgba(200,170,110,0.6)]`;

                    document.getElementById('modal-skill-slot').textContent = spell.slot;
                    document.getElementById('modal-skill-name').textContent = spell.name;
                    document.getElementById('modal-skill-desc').innerHTML = spell.description.replace(/(<br\s*\/?>\s*){2,}/gi, '<br><span class="block h-3"></span>');
                };
                skillsContainer.appendChild(btn);
            });

            document.getElementById('modal-skill-slot').textContent = champ.spells[0].slot;
            document.getElementById('modal-skill-name').textContent = champ.spells[0].name;
            document.getElementById('modal-skill-desc').innerHTML = champ.spells[0].description.replace(/(<br\s*\/?>\s*){2,}/gi, '<br><span class="block h-3"></span>');
        }
    }

    const modal = document.getElementById('champion-modal');
    if (modal) {
        modal.classList.remove('hidden');
        setTimeout(() => { modal.classList.remove('opacity-0'); }, 10);
    }
}

function closeModal() {
    const modal = document.getElementById('champion-modal');
    if (modal) {
        modal.classList.add('opacity-0');
        setTimeout(() => { modal.classList.add('hidden'); }, 300);
    }
}

function openSplashModal() {
    if (!currentModalChampId) return;
    const champ = allChampions.find(c => c.id === currentModalChampId);
    if (!champ) return;

    const splashImg = document.getElementById('splash-img');
    const targetImg = splashImg || document.getElementById('splash-modal-img');

    if (targetImg) {
        targetImg.src = champ.splashPath || champ.imagePath;

        targetImg.onerror = function () {
            const officialUrl = `https://ddragon.leagueoflegends.com/cdn/img/champion/splash/${champ.champKey}_0.jpg`;
            if (this.src !== officialUrl) this.src = officialUrl;
            else { this.onerror = null; this.src = champ.imagePath; }
        }
    }

    const splashName = document.getElementById('splash-name');
    if (splashName) {
        splashName.textContent = champ.name;
    }

    const splashModal = document.getElementById('splash-modal');
    if (splashModal) {
        splashModal.classList.remove('hidden');
        setTimeout(() => { splashModal.classList.remove('opacity-0'); }, 10);
    }
}

function closeSplashModal() {
    const splashModal = document.getElementById('splash-modal');
    if (splashModal) {
        splashModal.classList.add('opacity-0');
        setTimeout(() => { splashModal.classList.add('hidden'); }, 300);
    }
}

function openSplashFromResult(champId) {
    currentModalChampId = champId;
    openSplashModal();
}

document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
        closeModal();
        closeSplashModal();
    }
});