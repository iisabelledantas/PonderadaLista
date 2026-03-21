import http from "k6/http";
import { check, sleep } from "k6";
import { Counter, Rate, Trend } from "k6/metrics";

const mensagensEnviadas   = new Counter("mensagens_enviadas");
const mensagensComErro    = new Counter("mensagens_com_erro");
const taxaErros           = new Rate("taxa_erros");
const tempoPorRequisicao  = new Trend("tempo_por_requisicao", true);


export const options = {
  scenarios: {

    warm_up: {
      executor: "constant-vus",
      vus: 10,
      duration: "15s",
      tags: { fase: "warm_up" },
    },

    carga_leve: {
      executor: "ramping-vus",
      startVUs: 10,
      stages: [
        { duration: "10s", target: 50 },  // sobe gradualmente
        { duration: "20s", target: 50 },  // mantém
        { duration: "10s", target: 0  },  // desce
      ],
      startTime: "15s",
      tags: { fase: "carga_leve" },
    },


    pico: {
      executor: "constant-vus",
      vus: 100,
      duration: "15s",
      startTime: "55s",
      tags: { fase: "pico" },
    },
  },


  thresholds: {
    http_req_duration:    ["p(95)<3000"],
    taxa_erros:           ["rate<0.05"],
    mensagens_com_erro:   ["count<25"],
  },
};


function gerarPayload() {
  const sensores = ["sensor-001", "sensor-002", "sensor-003", "sensor-004"];
  const sensorId = sensores[Math.floor(Math.random() * sensores.length)];

  return JSON.stringify({
    sensorId:    sensorId,
    temperatura: parseFloat((Math.random() * 40 + 10).toFixed(2)),
    umidade:     parseFloat((Math.random() * 60 + 20).toFixed(2)),
    timestamp:   new Date().toISOString(),
  });
}


const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const ROTA     = `${BASE_URL}/api/telemetria/dados`;

const HEADERS  = {
  "Content-Type": "application/json",
};

export default function () {
  const payload = gerarPayload();
  const inicio  = Date.now();

  const res = http.post(ROTA, payload, {
    headers: HEADERS,
    timeout: "10s",
  });

  const duracao = Date.now() - inicio;
  tempoPorRequisicao.add(duracao);
  mensagensEnviadas.add(1);

  const sucesso = check(res, {
    "status 200 ou 202":     (r) => r.status === 200 || r.status === 202,
    "resposta em menos 3s":  (r) => r.timings.duration < 3000,
    "sem erro no body":      (r) => !r.body?.toLowerCase().includes("error"),
  });

  if (!sucesso) {
    mensagensComErro.add(1);
    taxaErros.add(1);
    console.error(`[ERRO] Status: ${res.status} | Body: ${res.body} | Fase: ${__ENV.K6_SCENARIO_NAME}`);
  } else {
    taxaErros.add(0);
  }

  sleep(0.5);
}


export function handleSummary(data) {
  const total   = data.metrics.mensagens_enviadas?.values?.count   ?? 0;
  const erros   = data.metrics.mensagens_com_erro?.values?.count   ?? 0;
  const p95     = data.metrics.tempo_por_requisicao?.values?.["p(95)"] ?? 0;
  const p99     = data.metrics.tempo_por_requisicao?.values?.["p(99)"] ?? 0;
  const mediana = data.metrics.tempo_por_requisicao?.values?.med   ?? 0;

  const resumo = `
╔══════════════════════════════════════════════════════╗
║              RESULTADO DO TESTE DE CARGA             ║
╠══════════════════════════════════════════════════════╣
║  Mensagens enviadas : ${String(total).padEnd(28)}║
║  Erros              : ${String(erros).padEnd(28)}║
║  Taxa de erro       : ${(erros / total * 100).toFixed(2).padEnd(27)}%║
╠══════════════════════════════════════════════════════╣
║  Tempo mediano      : ${String(mediana.toFixed(0) + " ms").padEnd(28)}║
║  p95 (95% abaixo)   : ${String(p95.toFixed(0) + " ms").padEnd(28)}║
║  p99 (99% abaixo)   : ${String(p99.toFixed(0) + " ms").padEnd(28)}║
╚══════════════════════════════════════════════════════╝
`;

  console.log(resumo);

  return {
    "stdout":           resumo,
    "resultado.json":   JSON.stringify(data, null, 2),
  };
}